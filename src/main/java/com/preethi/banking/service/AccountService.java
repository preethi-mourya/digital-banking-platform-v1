package com.preethi.banking.service;

import com.preethi.banking.entity.Account;
import com.preethi.banking.entity.AccountStatus;
import com.preethi.banking.entity.Customer;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.AccountRepository;
import com.preethi.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Account createAccount(Long customerId, Account account) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId));

        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Opening balance cannot be negative");
        }

        account.setAccountNumber(generateAccountNumber());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    public Account getAccountById(Long id) {

        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id));
    }

    public Account getAccountByNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with number: "
                                        + accountNumber));
    }

    public List<Account> getAccountsByCustomer(Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        }

        return accountRepository.findByCustomerId(customerId);
    }

    public Account blockAccount(Long id) {

        Account account = getAccountById(id);

        account.setStatus(AccountStatus.BLOCKED);

        return accountRepository.save(account);
    }

    public Account closeAccount(Long id) {

        Account account = getAccountById(id);

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Account cannot be closed while balance is not zero");
        }

        account.setStatus(AccountStatus.CLOSED);

        return accountRepository.save(account);
    }

    private String generateAccountNumber() {

        String accountNumber;

        do {
            accountNumber = "AC"
                    + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}