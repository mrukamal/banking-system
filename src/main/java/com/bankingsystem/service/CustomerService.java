package com.bankingsystem.service;

import com.bankingsystem.entity.CustomerEntity;
import com.bankingsystem.exception.BankingException;
import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.dto.CustomerDTO;
import com.bankingsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
        // accounts list is not persisted here; leave default empty
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
        // Default active to true if it's a new entity or preserve it if we had it
        return entity;
    }

    public List<CustomerDTO> getAllCustomers() {
        // Map all active customers first
        List<CustomerDTO> customers = customerRepository.findAll()
                .stream()
                .filter(CustomerEntity::getActive)
                .map(this::toDto)
                .collect(Collectors.toList());

        // Load all active accounts and group by account holder name to attach to customers
        List<AccountDTO> accounts = accountService.getAllAccounts(); // already filtered in accountService
        Map<String, List<AccountDTO>> accountsByHolder = accounts.stream()
                .collect(Collectors.groupingBy(AccountDTO::getAccountHolderName));

        customers.forEach(c -> c.setAccounts(
                new ArrayList<>(accountsByHolder.getOrDefault(c.getName(), new ArrayList<>()))
        ));

        return customers;
    }

    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
                .filter(CustomerEntity::getActive)
                .map(entity -> {
                    CustomerDTO customer = toDto(entity);
                    // Attach associated active accounts by matching account holder name
                    List<AccountDTO> accountsForCustomer = accountService.getAllAccounts() // already filtered in accountService
                            .stream()
                            .filter(a -> customer.getName() != null && customer.getName().equals(a.getAccountHolderName()))
                            .collect(Collectors.toList());
                    customer.setAccounts(accountsForCustomer);
                    return customer;
                })
                .orElseThrow(() -> new BankingException("Customer with id " + id + " not found", HttpStatus.NOT_FOUND));
    }

    public CustomerDTO createCustomer(CustomerDTO customer) {
        // Basic request validation
        if (customer == null) {
            throw new BankingException("Customer body is required", HttpStatus.BAD_REQUEST);
        }

        String customerName = customer.getName();
        if (customerName == null || customerName.isBlank()) {
            throw new BankingException("Customer.name must not be null or blank", HttpStatus.BAD_REQUEST);
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
                String holder = account.getAccountHolderName();
                if (holder == null || holder.isBlank()) {
                    throw new BankingException("Account.accountHolderName must not be null or blank when accounts are provided", HttpStatus.BAD_REQUEST);
                }
                if (!customerName.equals(holder)) {
                    throw new BankingException("Account.accountHolderName must match Customer.name", HttpStatus.BAD_REQUEST);
                }
            }
        }
        customer.setActive(true);
        CustomerEntity saved = customerRepository.save(toEntity(customer));
        return toDto(saved);
    }

    public CustomerDTO updateCustomer(Long id, CustomerDTO customer) {
        if (!customerRepository.existsById(id)) {
            throw new BankingException("Customer with id " + id + " not found", HttpStatus.NOT_FOUND);
        }
        customer.setId(id);
        CustomerEntity updated = customerRepository.save(toEntity(customer));
        return toDto(updated);
    }

    public void deleteCustomer(Long id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new BankingException("Customer with id " + id + " not found", HttpStatus.NOT_FOUND));
        customer.setActive(false);
        customerRepository.save(customer);
    }
}