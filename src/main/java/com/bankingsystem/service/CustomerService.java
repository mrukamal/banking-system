package com.bankingsystem.service;

import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.dto.CustomerDTO;
import com.bankingsystem.entity.AccountEntity;
import com.bankingsystem.entity.CustomerEntity;
import com.bankingsystem.exception.BankingException;
import com.bankingsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountService accountService;

    private CustomerDTO toDto(CustomerEntity entity) {
        if (entity == null) return null;
        CustomerDTO customer = new CustomerDTO();
        customer.setId(entity.getId());
        customer.setName(entity.getName());
        customer.setEmail(entity.getEmail());
        customer.setActive(entity.getActive());
        customer.setCreatedAt(entity.getCreatedAt());
        customer.setLastModifiedAt(entity.getLastModifiedAt());
        if (entity.getAccounts() != null) {
            customer.setAccounts(entity.getAccounts().stream()
                    .map(accountService::toDto)
                    .collect(Collectors.toList())
            );
        }
        return customer;
    }

    private CustomerEntity toEntity(CustomerDTO customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(customer.getId());
        entity.setName(customer.getName());
        entity.setEmail(customer.getEmail());
        if (customer.getActive() != null) {
            entity.setActive(customer.getActive());
        }
        if (customer.getAccounts() != null) {
            entity.setAccounts(customer.getAccounts().stream()
                    .map(accountService::toEntity)
                    .peek(account -> account.setCustomer(entity))
                    .collect(Collectors.toList()));
        }
        return entity;
    }

    public List<CustomerDTO> getAllCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        return customerRepository.findBy(pageable)
                .stream()
                .filter(CustomerEntity::getActive)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
                .filter(CustomerEntity::getActive)
                .map(this::toDto)
                .orElseThrow(() -> new BankingException("Customer with id " + id + " not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customer) {
        // Basic request validation
        if (customer == null) {
            throw new BankingException("Request body is empty", HttpStatus.BAD_REQUEST);
        }

        String customerName = customer.getName();
        if (customerName == null || customerName.isBlank()) {
            throw new BankingException("Customer name must not be null or blank", HttpStatus.BAD_REQUEST);
        }

        if (customerRepository.existsByNameAndActiveTrue(customerName)) {
            throw new BankingException("Customer with name " + customerName + " already exists", HttpStatus.BAD_REQUEST);
        }

        List<AccountDTO> accounts = customer.getAccounts();
        if (accounts != null && !accounts.isEmpty()) {
            for (AccountDTO account : accounts) {
                if (account == null) {
                    throw new BankingException("Account entry must not be null when accounts are provided", HttpStatus.BAD_REQUEST);
                }
                if (account.getAccountType() == null) {
                    throw new BankingException("Account Type cannot be blank", HttpStatus.BAD_REQUEST);
                }
            }
        }
        customer.setActive(true);
        CustomerEntity saved = customerRepository.save(toEntity(customer));
        return toDto(saved);
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customer) {
        CustomerEntity existing = customerRepository.findById(id)
                .orElseThrow(() -> new BankingException("Customer with id " + id + " not found", HttpStatus.NOT_FOUND));
        
        existing.setName(customer.getName());
        existing.setEmail(customer.getEmail());
        if (customer.getActive() != null) {
            existing.setActive(customer.getActive());
        }
        
        // Note: we don't update accounts here as it's a separate concern or handled via cascade if needed
        // but the current implementation of updateCustomer in the original code replaced the entity.
        
        CustomerEntity updated = customerRepository.save(existing);
        return toDto(updated);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new BankingException("Customer with id " + id + " not found", HttpStatus.NOT_FOUND));
        if(customer.getAccounts().isEmpty()
        || customer.getAccounts().stream().noneMatch(AccountEntity::getActive)) {
            customer.setActive(false);
            customerRepository.save(customer);
        } else {
            throw new BankingException("Customer id " + id + " has existing accounts and cannot be inactivated", HttpStatus.CONFLICT);
        }
    }
}