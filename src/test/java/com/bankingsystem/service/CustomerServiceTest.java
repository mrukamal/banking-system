package com.bankingsystem.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;

import com.bankingsystem.dto.AccountType;
import com.bankingsystem.entity.AccountEntity;
import com.bankingsystem.entity.CustomerEntity;
import com.bankingsystem.exception.BankingException;
import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.dto.CustomerDTO;
import com.bankingsystem.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.*;

public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Helper methods to create entities and DTOs
    private CustomerEntity createCustomerEntity(Long id, String name, String email, boolean active) {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setEmail(email);
        entity.setActive(active);
        entity.setAccounts(new ArrayList<>());
        return entity;
    }

    private CustomerDTO createCustomerDTO(Long id, String name, String email, Boolean active) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        dto.setActive(active);
        return dto;
    }

    private AccountDTO createAccountDTO() {
        AccountDTO account = new AccountDTO();
        return account;
    }

    @Test
    public void testGetAllCustomers_returnsActiveCustomersWithAccounts() {
        // Arrange
        CustomerEntity activeCustomer = createCustomerEntity(1L, "John Doe", "john@example.com", true);
        com.bankingsystem.entity.AccountEntity accountEntity = new com.bankingsystem.entity.AccountEntity();
        accountEntity.setId(10L);
        accountEntity.setActive(true);
        accountEntity.setCustomer(activeCustomer);
        activeCustomer.setAccounts(java.util.Collections.singletonList(accountEntity));

        CustomerEntity inactiveCustomer = createCustomerEntity(2L, "Jane Doe", "jane@example.com", false);

        when(customerRepository.findAll()).thenReturn(Arrays.asList(activeCustomer, inactiveCustomer));

        // Act
        List<CustomerDTO> result = customerService.getAllCustomers();

        // Assert
        assertEquals(1, result.size());
        CustomerDTO customer = result.get(0);
        assertEquals("John Doe", customer.getName());
        assertEquals(1, customer.getAccounts().size());
        verify(customerRepository).findAll();
    }

    @Test
    public void testGetCustomerById_foundWithAccounts() {
        // Arrange
        Long id = 1L;
        CustomerEntity entity = createCustomerEntity(id, "Alice", "alice@example.com", true);
        com.bankingsystem.entity.AccountEntity accountEntity = new com.bankingsystem.entity.AccountEntity();
        accountEntity.setId(20L);
        accountEntity.setActive(true);
        accountEntity.setCustomer(entity);
        entity.setAccounts(java.util.Collections.singletonList(accountEntity));
        
        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        CustomerDTO result = customerService.getCustomerById(id);

        // Assert
        assertNotNull(result);
        assertEquals("Alice", result.getName());
        assertEquals(1, result.getAccounts().size());
        verify(customerRepository).findById(id);
    }

    @Test
    public void testGetCustomerById_notFound_throwsException() {
        // Arrange
        Long id = 99L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        BankingException exception = assertThrows(BankingException.class, () -> customerService.getCustomerById(id));
        assertEquals("Customer with id 99 not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    public void testCreateCustomer_success() {
        // Arrange
        CustomerDTO dto = createCustomerDTO(null, "Bob", "bob@example.com", null);
        when(customerRepository.existsByNameAndActiveTrue("Bob")).thenReturn(false);
        CustomerEntity savedEntity = createCustomerEntity(1L, "Bob", "bob@example.com", true);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(savedEntity);

        // Act
        CustomerDTO result = customerService.createCustomer(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Bob", result.getName());
        assertTrue(result.getActive());
        verify(customerRepository).existsByNameAndActiveTrue("Bob");
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    public void testCreateCustomer_duplicateName_throws() {
        // Arrange
        CustomerDTO dto = createCustomerDTO(null, "Alice", "alice@example.com", null);
        when(customerRepository.existsByNameAndActiveTrue("Alice")).thenReturn(true);

        // Act & Assert
        BankingException ex = assertThrows(BankingException.class, () -> customerService.createCustomer(dto));
        assertEquals("Customer with name Alice already exists", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    public void testCreateCustomer_nullBody_throws() {
        // Act & Assert
        BankingException ex = assertThrows(BankingException.class, () -> customerService.createCustomer(null));
        assertEquals("Request body is empty", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    public void testCreateCustomer_blankName_throws() {
        // Arrange
        CustomerDTO dto = createCustomerDTO(null, " ", "test@example.com", null);

        // Act & Assert
        BankingException ex = assertThrows(BankingException.class, () -> customerService.createCustomer(dto));
        assertEquals("Customer name must not be null or blank", ex.getMessage());
    }

    @Test
    public void testCreateCustomer_accountsWithNulls_throws() {
        // Arrange
        CustomerDTO dto = createCustomerDTO(null, "Charlie", "charlie@example.com", null);
        List<AccountDTO> accounts = Arrays.asList(
                null,
                createAccountDTO()
        );
        dto.setAccounts(accounts);
        when(customerRepository.existsByNameAndActiveTrue("Charlie")).thenReturn(false);

        // Act & Assert
        BankingException ex = assertThrows(BankingException.class, () -> customerService.createCustomer(dto));
        assertEquals("Account entry must not be null when accounts are provided", ex.getMessage());
    }


    @Test
    public void testUpdateCustomer_success() {
        // Arrange
        Long id = 1L;
        CustomerDTO dto = createCustomerDTO(id, "Eve", "eve@example.com", true);
        CustomerEntity existingEntity = createCustomerEntity(id, "Old Eve", "old_eve@example.com", true);
        when(customerRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        CustomerEntity savedEntity = createCustomerEntity(id, "Eve", "eve@example.com", true);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(savedEntity);

        // Act
        CustomerDTO result = customerService.updateCustomer(id, dto);

        // Assert
        assertNotNull(result);
        assertEquals("Eve", result.getName());
        verify(customerRepository).findById(id);
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    public void testUpdateCustomer_notFound_throws() {
        // Arrange
        Long id = 999L;
        CustomerDTO dto = createCustomerDTO(id, "Ghost", "ghost@example.com", true);
        when(customerRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        BankingException ex = assertThrows(BankingException.class, () -> customerService.updateCustomer(id, dto));
        assertEquals("Customer with id 999 not found", ex.getMessage());
    }

    @Test
    public void testUpdateCustomer_retainsCreatedAt() {
        // Arrange
        Long id = 1L;
        java.util.Date createdAt = new java.util.Date();
        CustomerEntity existingEntity = createCustomerEntity(id, "Old Name", "old@example.com", true);
        existingEntity.setCreatedAt(createdAt);

        CustomerDTO updateDto = createCustomerDTO(id, "New Name", "new@example.com", true);

        when(customerRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CustomerDTO result = customerService.updateCustomer(id, updateDto);

        // Assert
        assertEquals("New Name", result.getName());
        assertNotNull(result.getCreatedAt(), "CreatedAt should not be null in response");
        assertEquals(createdAt, result.getCreatedAt());
    }

    @Test
    public void testDeleteCustomer_success() {
        // Arrange
        Long id = 1L;
        CustomerEntity entity = createCustomerEntity(id, "Henry", "henry@example.com", true);
        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        customerService.deleteCustomer(id);

        // Assert
        verify(customerRepository).findById(id);
        verify(customerRepository).save(argThat(c -> c.getActive() == false));
    }

    @Test
    public void testDeleteCustomerWithInactiveAccounts_success() {
        // Arrange
        Long id = 1L;

        CustomerEntity entity = new CustomerEntity();
        entity.setId(id);
        entity.setName("Henry");
        entity.setEmail("Henry@example.com");
        entity.setActive(true);
        List<AccountEntity> accounts = new ArrayList<>();
        AccountEntity accountEntity = AccountEntity.builder()
                .customer(entity)
                .active(false)
                .accountType(AccountType.CHECKING)
                .build();
        accounts.add(accountEntity);
        entity.setAccounts(accounts);
        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        customerService.deleteCustomer(id);

        // Assert
        verify(customerRepository).findById(id);
        verify(customerRepository).save(argThat(c -> c.getActive() == false));
    }

    @Test
    public void testDeleteCustomerWithActiveAccounts_failure() {
        // Arrange
        Long id = 2L;
        CustomerEntity entity = new CustomerEntity();
        entity.setId(id);
        entity.setName("Henry");
        entity.setEmail("Henry@example.com");
        entity.setActive(true);
        List<AccountEntity> accounts = new ArrayList<>();
        AccountEntity accountEntity = AccountEntity.builder()
                .customer(entity)
                .active(true)
                .accountType(AccountType.CHECKING)
                .build();
        accounts.add(accountEntity);
        entity.setAccounts(accounts);
        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act & Assert
        BankingException ex = assertThrows(BankingException.class, () -> customerService.deleteCustomer(id));
        assertEquals("Customer id 2 has existing accounts and cannot be inactivated", ex.getMessage());
    }


    @Test
    public void testDeleteCustomer_notFound_throws() {
        // Arrange
        Long id = 2L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        BankingException ex = assertThrows(BankingException.class, () -> customerService.deleteCustomer(id));
        assertEquals("Customer with id 2 not found", ex.getMessage());
    }
}