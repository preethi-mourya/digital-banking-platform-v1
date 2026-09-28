package com.preethi.banking.service;

import com.preethi.banking.dto.TransferRequest;
import com.preethi.banking.dto.TransferResponse;
import com.preethi.banking.entity.Account;
import com.preethi.banking.entity.AccountStatus;
import com.preethi.banking.entity.BankTransaction;
import com.preethi.banking.entity.Beneficiary;
import com.preethi.banking.entity.BeneficiaryStatus;
import com.preethi.banking.entity.TransactionStatus;
import com.preethi.banking.entity.TransactionType;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.AccountRepository;
import com.preethi.banking.repository.BankTransactionRepository;
import com.preethi.banking.repository.BeneficiaryRepository;
import com.preethi.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            BeneficiaryRepository beneficiaryRepository,
            BankTransactionRepository bankTransactionRepository) {

        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    public Account createAccount(Long customerId, Account account) {

        var customer = customerRepository.findById(customerId)
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

    // =========================================================
    // GET ACCOUNT BY ID
    // =========================================================

    public Account getAccountById(Long id) {

        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id));
    }

    // =========================================================
    // GET ACCOUNT BY ACCOUNT NUMBER
    // =========================================================

    public Account getAccountByNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with number: "
                                        + accountNumber));
    }

    // =========================================================
    // GET ALL ACCOUNTS OF CUSTOMER
    // =========================================================

    public List<Account> getAccountsByCustomer(Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        }

        return accountRepository.findByCustomerId(customerId);
    }

    // =========================================================
    // BLOCK ACCOUNT
    // =========================================================

    public Account blockAccount(Long id) {

        Account account = getAccountById(id);

        account.setStatus(AccountStatus.BLOCKED);

        return accountRepository.save(account);
    }

    // =========================================================
    // CLOSE ACCOUNT
    // =========================================================

    public Account closeAccount(Long id) {

        Account account = getAccountById(id);

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Account cannot be closed while balance is not zero");
        }

        account.setStatus(AccountStatus.CLOSED);

        return accountRepository.save(account);
    }

    // =========================================================
    // FUND TRANSFER
    // =========================================================

    @Transactional
    public TransferResponse transferMoney(
            Long customerId,
            TransferRequest request) {

        // 1. Find source account
        Account sourceAccount =
                accountRepository.findByAccountNumber(
                                request.getSourceAccountNumber())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Source account not found"));

        // 2. Verify source account belongs to customer
        if (!sourceAccount.getCustomer().getId()
                .equals(customerId)) {

            throw new IllegalArgumentException(
                    "Source account does not belong to customer");
        }

        // 3. Source account must be active
        if (sourceAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Source account is not active");
        }

        // 4. Find beneficiary
        Beneficiary beneficiary =
                beneficiaryRepository.findById(
                                request.getBeneficiaryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Beneficiary not found"));

        // 5. Verify beneficiary belongs to customer
        if (!beneficiary.getCustomer().getId()
                .equals(customerId)) {

            throw new IllegalArgumentException(
                    "Beneficiary does not belong to customer");
        }

        // 6. Beneficiary must be active
        if (beneficiary.getStatus()
                != BeneficiaryStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Beneficiary is not active");
        }

        // 7. Find destination account
        Account destinationAccount =
                accountRepository.findByAccountNumber(
                                beneficiary.getAccountNumber())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Destination account not found"));

        // 8. Source and destination cannot be same
        if (sourceAccount.getAccountNumber()
                .equals(destinationAccount.getAccountNumber())) {

            throw new IllegalArgumentException(
                    "Source and destination accounts cannot be the same");
        }

        // 9. Validate amount
        BigDecimal amount = request.getAmount();

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        // 10. Check balance
        if (sourceAccount.getBalance()
                .compareTo(amount) < 0) {

            throw new IllegalStateException(
                    "Insufficient account balance");
        }

        // 11. Debit source account
        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(amount));

        // 12. Credit destination account
        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(amount));

        // 13. Generate transaction reference
        String reference =
                "TXN-" +
                        UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 16)
                                .toUpperCase();

        // 14. Create transaction record
        BankTransaction transaction =
                new BankTransaction();

        transaction.setTransactionReference(reference);

        transaction.setSourceAccountNumber(
                sourceAccount.getAccountNumber());

        transaction.setDestinationAccountNumber(
                destinationAccount.getAccountNumber());

        transaction.setAmount(amount);

        transaction.setType(
                TransactionType.TRANSFER);

        transaction.setStatus(
                TransactionStatus.SUCCESS);

        transaction.setCreatedAt(
                LocalDateTime.now());

        // 15. Save transaction
        bankTransactionRepository.save(transaction);

        // 16. Return response
        return new TransferResponse(
                reference,
                sourceAccount.getAccountNumber(),
                destinationAccount.getAccountNumber(),
                amount,
                TransactionStatus.SUCCESS.name(),
                "Fund transfer completed successfully");
    }

    // =========================================================
    // TRANSACTION HISTORY
    // =========================================================

    public List<BankTransaction> getTransactionHistory(
            String accountNumber) {

        List<BankTransaction> outgoing =
                bankTransactionRepository
                        .findBySourceAccountNumberOrderByCreatedAtDesc(
                                accountNumber);

        List<BankTransaction> incoming =
                bankTransactionRepository
                        .findByDestinationAccountNumberOrderByCreatedAtDesc(
                                accountNumber);

        List<BankTransaction> transactions =
                new ArrayList<>();

        transactions.addAll(outgoing);
        transactions.addAll(incoming);

        return transactions;
    }

    // =========================================================
    // GENERATE ACCOUNT NUMBER
    // =========================================================

    private String generateAccountNumber() {

        String accountNumber;

        do {
            accountNumber =
                    "AC" +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 12)
                                    .toUpperCase();

        } while (accountRepository
                .existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}