package com.preethi.banking.controller;

import com.preethi.banking.entity.Account;
import com.preethi.banking.entity.Customer;
import com.preethi.banking.service.AccountService;
import com.preethi.banking.service.CustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
public class EmployeeController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public EmployeeController(
            CustomerService customerService,
            AccountService accountService) {

        this.customerService = customerService;
        this.accountService = accountService;
    }

    @GetMapping("/customers")
    public List<Customer> getCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/customers/{customerId}")
    public Customer getCustomer(
            @PathVariable Long customerId) {

        return customerService.getCustomerById(customerId);
    }

    @GetMapping("/customers/{customerId}/accounts")
    public List<Account> getCustomerAccounts(
            @PathVariable Long customerId) {

        return accountService.getAccountsByCustomer(customerId);
    }
}