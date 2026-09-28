package com.preethi.banking.controller;

import com.preethi.banking.dto.TransferRequest;
import com.preethi.banking.dto.TransferResponse;
import com.preethi.banking.entity.Account;
import com.preethi.banking.entity.BankTransaction;
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

    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    @PostMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(
            @PathVariable Long customerId,
            @Valid @RequestBody Account account) {

        return accountService.createAccount(
                customerId,
                account);
    }

    // =========================================================
    // GET ACCOUNT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public Account getAccountById(
            @PathVariable Long id) {

        return accountService.getAccountById(id);
    }

    // =========================================================
    // GET ACCOUNT BY ACCOUNT NUMBER
    // =========================================================

    @GetMapping("/number/{accountNumber}")
    public Account getAccountByNumber(
            @PathVariable String accountNumber) {

        return accountService.getAccountByNumber(
                accountNumber);
    }

    // =========================================================
    // GET CUSTOMER ACCOUNTS
    // =========================================================

    @GetMapping("/customer/{customerId}")
    public List<Account> getAccountsByCustomer(
            @PathVariable Long customerId) {

        return accountService.getAccountsByCustomer(
                customerId);
    }

    // =========================================================
    // BLOCK ACCOUNT
    // =========================================================

    @PutMapping("/{id}/block")
    public Account blockAccount(
            @PathVariable Long id) {

        return accountService.blockAccount(id);
    }

    // =========================================================
    // CLOSE ACCOUNT
    // =========================================================

    @PutMapping("/{id}/close")
    public Account closeAccount(
            @PathVariable Long id) {

        return accountService.closeAccount(id);
    }

    // =========================================================
    // FUND TRANSFER
    // =========================================================

    @PostMapping("/transfer/customer/{customerId}")
    public TransferResponse transferMoney(
            @PathVariable Long customerId,
            @Valid @RequestBody TransferRequest request) {

        return accountService.transferMoney(
                customerId,
                request);
    }

    // =========================================================
    // TRANSACTION HISTORY
    // =========================================================

    @GetMapping("/{accountNumber}/transactions")
    public List<BankTransaction> getTransactionHistory(
            @PathVariable String accountNumber) {

        return accountService.getTransactionHistory(
                accountNumber);
    }
}