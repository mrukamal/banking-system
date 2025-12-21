package com.bankingsystem.service;

import com.bankingsystem.exception.BankingException;
import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_WithValidData_ShouldReturnAccount() {
        AccountDTO account = new AccountDTO();
        account.setAccountHolderName("John Doe");
        account.setAccountType("SAVINGS");
        account.setBalance(BigDecimal.ZERO);

        com.bankingsystem.entity.AccountEntity entity = new com.bankingsystem.entity.AccountEntity();
        entity.setId(1L);
        entity.setAccountHolderName("John Doe");
        entity.setAccountType("SAVINGS");
        entity.setBalance(BigDecimal.ZERO);

        when(customerRepository.existsByNameAndActiveTrue("John Doe")).thenReturn(true);
        when(accountRepository.save(any(com.bankingsystem.entity.AccountEntity.class))).thenReturn(entity);

        AccountDTO result = accountService.createAccount(account);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getAccountHolderName());
        assertEquals("SAVINGS", result.getAccountType());
    }

    @Test
    void getAccountById_ShouldReturnAccountWithAuditFields() {
        Long id = 1L;
        LocalDateTime now = LocalDateTime.now();
        com.bankingsystem.entity.AccountEntity entity = new com.bankingsystem.entity.AccountEntity();
        entity.setId(id);
        entity.setAccountHolderName("John Doe");
        entity.setAccountType("SAVINGS");
        entity.setBalance(BigDecimal.ZERO);
        entity.setCreatedAt(now);
        entity.setLastModifiedAt(now);

        when(accountRepository.findById(id)).thenReturn(java.util.Optional.of(entity));

        AccountDTO result = accountService.getAccountById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getLastModifiedAt());
    }

    @Test
    void createAccount_WithNullHolderName_ShouldThrowException() {
        AccountDTO account = new AccountDTO();
        account.setAccountHolderName(null);
        account.setAccountType("SAVINGS");

        BankingException exception = assertThrows(BankingException.class, () -> accountService.createAccount(account));

        assertEquals("Account holder name is required", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void createAccount_WithEmptyHolderName_ShouldThrowException() {
        AccountDTO account = new AccountDTO();
        account.setAccountHolderName("");
        account.setAccountType("SAVINGS");

        BankingException exception = assertThrows(BankingException.class, () -> accountService.createAccount(account));

        assertEquals("Account holder name is required", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void createAccount_WithInvalidAccountType_ShouldThrowException() {
        AccountDTO account = new AccountDTO();
        account.setAccountHolderName("John Doe");
        account.setAccountType("INVALID");

        when(customerRepository.existsByNameAndActiveTrue("John Doe")).thenReturn(true);

        BankingException exception = assertThrows(BankingException.class, () -> accountService.createAccount(account));

        assertEquals("Invalid account type. Must be SAVINGS or CHECKING", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void createAccount_WithNullAccountType_ShouldThrowException() {
        AccountDTO account = new AccountDTO();
        account.setAccountHolderName("John Doe");
        account.setAccountType(null);

        when(customerRepository.existsByNameAndActiveTrue("John Doe")).thenReturn(true);

        BankingException exception = assertThrows(BankingException.class, () -> accountService.createAccount(account));

        assertEquals("Invalid account type. Must be SAVINGS or CHECKING", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void createAccount_WithNonExistentCustomer_ShouldThrowException() {
        AccountDTO account = new AccountDTO();
        account.setAccountHolderName("nonexistent");
        account.setAccountType("SAVINGS");

        when(customerRepository.existsByNameAndActiveTrue("nonexistent")).thenReturn(false);

        BankingException exception = assertThrows(BankingException.class, () -> accountService.createAccount(account));

        assertEquals("Customer with name nonexistent does not exist", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void deleteAccount_ShouldSetActiveToFalse() {
        Long id = 1L;
        com.bankingsystem.entity.AccountEntity entity = new com.bankingsystem.entity.AccountEntity();
        entity.setId(id);
        entity.setActive(true);

        when(accountRepository.findById(id)).thenReturn(java.util.Optional.of(entity));

        accountService.deleteAccount(id);

        assertFalse(entity.getActive());
        verify(accountRepository).save(entity);
    }

    @Test
    void updateAccount_ShouldAllowReactivatingAccount() {
        Long id = 1L;
        com.bankingsystem.entity.AccountEntity existingEntity = new com.bankingsystem.entity.AccountEntity();
        existingEntity.setId(id);
        existingEntity.setAccountHolderName("John Doe");
        existingEntity.setAccountType("SAVINGS");
        existingEntity.setBalance(BigDecimal.ZERO);
        existingEntity.setActive(false);

        AccountDTO updateDto = new AccountDTO();
        updateDto.setAccountHolderName("John Doe");
        updateDto.setAccountType("SAVINGS");
        updateDto.setActive(true);

        when(accountRepository.existsById(id)).thenReturn(true);
        when(customerRepository.existsByNameAndActiveTrue("John Doe")).thenReturn(true);
        when(accountRepository.save(any(com.bankingsystem.entity.AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountDTO result = accountService.updateAccount(id, updateDto);

        assertNotNull(result);
        assertEquals(true, result.getActive());
    }
}
