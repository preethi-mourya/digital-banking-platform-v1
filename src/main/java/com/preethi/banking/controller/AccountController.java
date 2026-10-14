package com.preethi.banking.controller;

import com.preethi.banking.dto.TransferRequest;
import com.preethi.banking.dto.TransferResponse;
import com.preethi.banking.entity.Account;
import com.preethi.banking.entity.BankTransaction;
import com.preethi.banking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    private Long requireCustomerId(Principal principal) {
        return accountService.getCustomerIdForUsername(principal.getName());
    }

    private void verifyCustomerPath(Long requestedCustomerId, Long authenticatedCustomerId) {
        if (!authenticatedCustomerId.equals(requestedCustomerId)) {
            throw new AccessDeniedException(
                    "You are not authorized to access resources for this customer");
        }
    }

    // CREATE ACCOUNT — customerId in the URL must match the authenticated user.
    @PostMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(
            @PathVariable Long customerId,
            @Valid @RequestBody Account account,
            Principal principal) {

        Long authenticatedCustomerId = requireCustomerId(principal);
        verifyCustomerPath(customerId, authenticatedCustomerId);
        return accountService.createAccount(authenticatedCustomerId, account);
    }

    // GET ACCOUNT BY ID — only the owning customer can read it through this API.
    @GetMapping("/{id}")
    public Account getAccountById(@PathVariable Long id, Principal principal) {
        Long customerId = requireCustomerId(principal);
        return accountService.getAccountByIdForCustomer(id, customerId);
    }

    // GET ACCOUNT BY ACCOUNT NUMBER — only the owner can read it.
    @GetMapping("/number/{accountNumber}")
    public Account getAccountByNumber(
            @PathVariable String accountNumber,
            Principal principal) {

        Long customerId = requireCustomerId(principal);
        return accountService.getAccountByNumberForCustomer(accountNumber, customerId);
    }

    // GET CUSTOMER ACCOUNTS — path customerId must match the authenticated customer.
    @GetMapping("/customer/{customerId}")
    public List<Account> getAccountsByCustomer(
            @PathVariable Long customerId,
            Principal principal) {

        Long authenticatedCustomerId = requireCustomerId(principal);
        verifyCustomerPath(customerId, authenticatedCustomerId);
        return accountService.getAccountsByCustomer(authenticatedCustomerId);
    }

    // BLOCK ACCOUNT — owner-only through this customer API.
    @PutMapping("/{id}/block")
    public Account blockAccount(@PathVariable Long id, Principal principal) {
        Long customerId = requireCustomerId(principal);
        return accountService.blockAccount(id, customerId);
    }

    // CLOSE ACCOUNT — owner-only through this customer API.
    @PutMapping("/{id}/close")
    public Account closeAccount(@PathVariable Long id, Principal principal) {
        Long customerId = requireCustomerId(principal);
        return accountService.closeAccount(id, customerId);
    }

    // FUND TRANSFER — URL customerId must match authenticated customer.
    @PostMapping("/transfer/customer/{customerId}")
    public TransferResponse transferMoney(
            @PathVariable Long customerId,
            @Valid @RequestBody TransferRequest request,
            Principal principal) {

        Long authenticatedCustomerId = requireCustomerId(principal);
        verifyCustomerPath(customerId, authenticatedCustomerId);
        return accountService.transferMoney(authenticatedCustomerId, request);
    }

    // TRANSACTION HISTORY — verify ownership of the account number first.
    @GetMapping("/{accountNumber}/transactions")
    public List<BankTransaction> getTransactionHistory(
            @PathVariable String accountNumber,
            Principal principal) {

        Long customerId = requireCustomerId(principal);
        return accountService.getTransactionHistoryForCustomer(accountNumber, customerId);
    }
}
