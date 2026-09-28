package com.preethi.banking.controller;

import com.preethi.banking.entity.Account;
import com.preethi.banking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(
            @PathVariable Long customerId,
            @Valid @RequestBody Account account) {

        return accountService.createAccount(customerId, account);
    }

    @GetMapping("/{id}")
    public Account getAccountById(@PathVariable Long id) {

        return accountService.getAccountById(id);
    }

    @GetMapping("/number/{accountNumber}")
    public Account getAccountByNumber(
            @PathVariable String accountNumber) {

        return accountService.getAccountByNumber(accountNumber);
    }

    @GetMapping("/customer/{customerId}")
    public List<Account> getAccountsByCustomer(
            @PathVariable Long customerId) {

        return accountService.getAccountsByCustomer(customerId);
    }

    @PutMapping("/{id}/block")
    public Account blockAccount(@PathVariable Long id) {

        return accountService.blockAccount(id);
    }

    @PutMapping("/{id}/close")
    public Account closeAccount(@PathVariable Long id) {

        return accountService.closeAccount(id);
    }
}