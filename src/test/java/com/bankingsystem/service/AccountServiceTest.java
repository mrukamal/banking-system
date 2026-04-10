package com.bankingsystem.service;

import com.bankingsystem.dto.AccountType;
import com.bankingsystem.entity.AccountEntity;
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
        account.setCustomerId(1L);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.ZERO);

        com.bankingsystem.entity.AccountEntity entity = new com.bankingsystem.entity.AccountEntity();
        entity.setId(1L);
        entity.setAccountType(AccountType.SAVINGS);
        entity.setBalance(BigDecimal.ZERO);

        when(customerRepository.existsByIdAndActiveTrue(1L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(new com.bankingsystem.entity.CustomerEntity()));
        when(accountRepository.save(any(com.bankingsystem.entity.AccountEntity.class))).thenReturn(entity);

        AccountDTO result = accountService.createAccount(account);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
    }

    @Test
    void getAccountById_ShouldReturnAccountWithAuditFields() {
        Long id = 1L;
        java.util.Date now = new java.util.Date();
        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        entity.setAccountType(AccountType.SAVINGS);
        entity.setBalance(BigDecimal.ZERO);
        entity.setCreatedAt(now);
        entity.setLastModifiedAt(now);
        entity.setActive(true);

        when(accountRepository.findById(id)).thenReturn(java.util.Optional.of(entity));

        AccountDTO result = accountService.getAccountById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getLastModifiedAt());
    }

    @Test
    void createAccount_WithNullCustomerId_ShouldThrowException() {
        AccountDTO account = new AccountDTO();
        account.setCustomerId(null);
        account.setAccountType(AccountType.SAVINGS);

        BankingException exception = assertThrows(BankingException.class, () -> accountService.createAccount(account));

        assertEquals("Customer ID is required", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void createAccount_WithNonExistentCustomer_ShouldThrowException() {
        AccountDTO account = new AccountDTO();
        account.setCustomerId(999L);
        account.setAccountType(AccountType.SAVINGS);

        BankingException exception = assertThrows(BankingException.class, () -> accountService.createAccount(account));

        assertEquals("Customer with id 999 does not exist", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
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
    void updateAccount_ShouldRetainCreatedAt() {
        Long id = 1L;
        java.util.Date createdAt = new java.util.Date();
        com.bankingsystem.entity.AccountEntity existingEntity = new com.bankingsystem.entity.AccountEntity();
        existingEntity.setId(id);
        existingEntity.setAccountType(AccountType.SAVINGS);
        existingEntity.setBalance(BigDecimal.ZERO);
        existingEntity.setCreatedAt(createdAt);

        AccountDTO updateDto = new AccountDTO();
        updateDto.setCustomerId(1L);
        updateDto.setAccountType(AccountType.SAVINGS);
        updateDto.setBalance(BigDecimal.TEN);

        when(accountRepository.findById(id)).thenReturn(java.util.Optional.of(existingEntity));
        when(customerRepository.existsByIdAndActiveTrue(1L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(new com.bankingsystem.entity.CustomerEntity()));
        when(accountRepository.save(any(com.bankingsystem.entity.AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountDTO result = accountService.updateAccount(id, updateDto);

        assertNotNull(result);
        assertEquals(createdAt, result.getCreatedAt(), "CreatedAt should be retained");
    }

    @Test
    void updateAccount_ShouldAllowReactivatingAccount() {
        Long id = 1L;
        com.bankingsystem.entity.AccountEntity existingEntity = new com.bankingsystem.entity.AccountEntity();
        existingEntity.setId(id);
        existingEntity.setAccountType(AccountType.SAVINGS);
        existingEntity.setBalance(BigDecimal.ZERO);
        existingEntity.setActive(false);

        AccountDTO updateDto = new AccountDTO();
        updateDto.setCustomerId(1L);
        updateDto.setAccountType(AccountType.SAVINGS);
        updateDto.setActive(true);

        when(accountRepository.findById(id)).thenReturn(java.util.Optional.of(existingEntity));
        when(customerRepository.existsByIdAndActiveTrue(1L)).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(new com.bankingsystem.entity.CustomerEntity()));
        when(accountRepository.save(any(com.bankingsystem.entity.AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountDTO result = accountService.updateAccount(id, updateDto);

        assertNotNull(result);
        assertEquals(true, result.getActive());
    }
}
