package com.bankingsystem.controller;

import com.bankingsystem.dto.AccountDTO;
import com.bankingsystem.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountDTO> getAllAccounts(@RequestParam Integer page, @RequestParam Integer size) {
        return accountService.getAllAccounts(page, size);
    }

    @GetMapping("/{id}")
    public AccountDTO getAccount(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @PostMapping
    public AccountDTO createAccount(@RequestBody AccountDTO account) {
        return accountService.createAccount(account);
    }

    @PutMapping("/{id}")
    public AccountDTO updateAccount(@PathVariable Long id, @RequestBody AccountDTO account) {
        return accountService.updateAccount(id, account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}