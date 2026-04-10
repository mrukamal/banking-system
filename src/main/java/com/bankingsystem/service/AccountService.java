package com.bankingsystem.service;

import com.bankingsystem.entity.AccountEntity;
import com.bankingsystem.exception.BankingException;
import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    private final CustomerRepository customerRepository;

    // Convert Entity to DTO
    public AccountDTO toDto(AccountEntity entity) {
        AccountDTO account = new AccountDTO();
        account.setId(entity.getId());
        if (entity.getCustomer() != null) {
            account.setCustomerId(entity.getCustomer().getId());
        }
        account.setAccountType(entity.getAccountType());
        account.setBalance(entity.getBalance());
        account.setActive(entity.getActive());
        account.setCreatedAt(entity.getCreatedAt());
        account.setLastModifiedAt(entity.getLastModifiedAt());
        return account;
    }

    // Convert Model to Entity
    public AccountEntity toEntity(AccountDTO account) {
        AccountEntity entity = new AccountEntity();
        entity.setId(account.getId());
        checkIfAccountExists(account, entity);
        return entity;
    }

    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .filter(AccountEntity::getActive)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AccountDTO getAccountById(Long id) {
        return accountRepository.findById(id)
                .filter(AccountEntity::getActive)
                .map(this::toDto)
                .orElseThrow(() -> new BankingException("Account with id " + id + " not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public AccountDTO createAccount(AccountDTO account) {
        validateAccount(account);
        account.setActive(true);
        AccountEntity entity = toEntity(account);
        AccountEntity saved = accountRepository.save(entity);
        return toDto(saved);
    }

    private void validateAccountForUpdate(AccountDTO account) {
        if (account.getCustomerId() == null) {
            throw new BankingException("Customer ID is required", HttpStatus.BAD_REQUEST);
        }
        if (!customerRepository.existsByIdAndActiveTrue(account.getCustomerId())) {
            throw new BankingException("Customer with id " + account.getCustomerId() + " does not exist", HttpStatus.NOT_FOUND);
        }
        if (account.getAccountType() == null) {
            throw new BankingException("Invalid account type. Must be SAVINGS or CHECKING", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateAccount(AccountDTO account) {
        validateAccountForUpdate(account);
        if (accountRepository.existsAccountEntitiesByCustomerIdAndActiveTrueAndAccountType(account.getCustomerId(), account.getAccountType())) {
            throw new BankingException("A " + account.getAccountType().name() + " Account for customer id " + account.getCustomerId() + " already exists", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public AccountDTO updateAccount(Long id, AccountDTO account) {
        AccountEntity existing = accountRepository.findById(id)
                .orElseThrow(() -> new BankingException("Account with id " + id + " not found", HttpStatus.NOT_FOUND));

        validateAccountForUpdate(account);

        checkIfAccountExists(account, existing);

        AccountEntity updated = accountRepository.save(existing);
        return toDto(updated);
    }

    private void checkIfAccountExists(AccountDTO account, AccountEntity existing) {
        if (account.getCustomerId() != null) {
            existing.setCustomer(customerRepository.findById(account.getCustomerId())
                    .orElseThrow(() -> new BankingException("Customer with id " + account.getCustomerId() + " not found", HttpStatus.NOT_FOUND)));
        }
        existing.setAccountType(account.getAccountType());
        existing.setBalance(account.getBalance());
        if (account.getActive() != null) {
            existing.setActive(account.getActive());
        }
    }

    @Transactional
    public void deleteAccount(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new BankingException("Account with id " + id + " not found", HttpStatus.NOT_FOUND));
        account.setActive(false);
        accountRepository.save(account);
    }
}