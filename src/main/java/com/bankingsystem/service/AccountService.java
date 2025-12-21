package com.bankingsystem.service;

import com.bankingsystem.entity.AccountEntity;
import com.bankingsystem.exception.BankingException;
import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final CustomerRepository customerRepository;

    // Convert Entity to DTO
    private AccountDTO toDto(AccountEntity entity) {
        AccountDTO account = new AccountDTO();
        account.setId(entity.getId());
        account.setAccountHolderName(entity.getAccountHolderName());
        account.setAccountType(entity.getAccountType());
        account.setBalance(entity.getBalance());
        account.setActive(entity.getActive());
        account.setCreatedAt(entity.getCreatedAt());
        account.setLastModifiedAt(entity.getLastModifiedAt());
        return account;
    }

    // Convert Model to Entity
    private AccountEntity toEntity(AccountDTO account) {
        AccountEntity entity = new AccountEntity();
        entity.setId(account.getId());
        entity.setAccountHolderName(account.getAccountHolderName());
        entity.setAccountType(account.getAccountType());
        entity.setBalance(account.getBalance());
        if (account.getActive() != null) {
            entity.setActive(account.getActive());
        }
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

    public AccountDTO createAccount(AccountDTO account) {
        validateAccount(account);
        account.setActive(true);
        AccountEntity entity = toEntity(account);
        AccountEntity saved = accountRepository.save(entity);
        return toDto(saved);
    }

    private void validateAccountForUpdate(AccountDTO account) {
        if (account.getAccountHolderName() == null || account.getAccountHolderName().trim().isEmpty()) {
            throw new BankingException("Account holder name is required", HttpStatus.BAD_REQUEST);
        }
        if (!customerRepository.existsByNameAndActiveTrue(account.getAccountHolderName())) {
            throw new BankingException("Customer with name " + account.getAccountHolderName() + " does not exist", HttpStatus.BAD_REQUEST);
        }
        if (account.getAccountType() == null || 
            (!account.getAccountType().equals("SAVINGS") && !account.getAccountType().equals("CHECKING"))) {
            throw new BankingException("Invalid account type. Must be SAVINGS or CHECKING", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateAccount(AccountDTO account) {
        validateAccountForUpdate(account);
        if (accountRepository.existsByAccountHolderNameAndActiveTrue(account.getAccountHolderName())) {
            throw new BankingException("Account with holder name " + account.getAccountHolderName() + " already exists", HttpStatus.BAD_REQUEST);
        }
    }

    public AccountDTO updateAccount(Long id, AccountDTO account) {
        if (!accountRepository.existsById(id)) {
            throw new BankingException("Account with id " + id + " not found", HttpStatus.NOT_FOUND);
        }
        validateAccountForUpdate(account);
        account.setId(id);
        AccountEntity entity = toEntity(account);
        AccountEntity updated = accountRepository.save(entity);
        return toDto(updated);
    }

    public void deleteAccount(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new BankingException("Account with id " + id + " not found", HttpStatus.NOT_FOUND));
        account.setActive(false);
        accountRepository.save(account);
    }
}