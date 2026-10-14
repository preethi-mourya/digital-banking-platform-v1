 package com.preethi.banking.service;

import com.preethi.banking.dto.TransferRequest;
import com.preethi.banking.dto.TransferResponse;
import com.preethi.banking.entity.Account;
import com.preethi.banking.entity.AccountStatus;
import com.preethi.banking.entity.AuditAction;
import com.preethi.banking.entity.BankTransaction;
import com.preethi.banking.entity.Beneficiary;
import com.preethi.banking.entity.BeneficiaryStatus;
import com.preethi.banking.entity.NotificationType;
import com.preethi.banking.entity.TransactionStatus;
import com.preethi.banking.entity.TransactionType;
import com.preethi.banking.entity.User;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.AccountRepository;
import com.preethi.banking.repository.BankTransactionRepository;
import com.preethi.banking.repository.BeneficiaryRepository;
import com.preethi.banking.repository.CustomerRepository;
import com.preethi.banking.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            BeneficiaryRepository beneficiaryRepository,
            BankTransactionRepository bankTransactionRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            AuditLogService auditLogService) {

        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.bankTransactionRepository = bankTransactionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    // GET CUSTOMER ID FROM LOGGED-IN USER

    @Transactional(readOnly = true)
    public Long getCustomerIdForUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username));

        if (user.getCustomer() == null) {
            throw new IllegalStateException(
                    "Logged-in user is not linked to a customer");
        }

        return user.getCustomer().getId();
    }

    // CREATE ACCOUNT

    @Transactional
    public Account createAccount(Long customerId, Account account) {

        var customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId));

        if (account.getAccountType() == null) {
            throw new IllegalArgumentException(
                    "Account type is required");
        }

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

        Account savedAccount = accountRepository.save(account);

        auditLogService.record(
                currentUsername(),
                AuditAction.ACCOUNT_CREATED,
                "Account",
                String.valueOf(savedAccount.getId()),
                "Account created for customer ID " + customerId);

        return savedAccount;
    }

    // GET ACCOUNT BY ID

    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {

        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id));
    }

    // GET ACCOUNT BY ID WITH CUSTOMER OWNERSHIP CHECK

    @Transactional(readOnly = true)
    public Account getAccountByIdForCustomer(
            Long id, Long customerId) {

        Account account = getAccountById(id);
        ensureOwnership(account, customerId);
        return account;
    }

    // GET ACCOUNT BY ACCOUNT NUMBER

    @Transactional(readOnly = true)
    public Account getAccountByNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with number: "
                                        + accountNumber));
    }

    // GET ACCOUNT BY NUMBER WITH CUSTOMER OWNERSHIP CHECK

    @Transactional(readOnly = true)
    public Account getAccountByNumberForCustomer(
            String accountNumber, Long customerId) {

        Account account = getAccountByNumber(accountNumber);
        ensureOwnership(account, customerId);
        return account;
    }

    // GET ALL ACCOUNTS OF CUSTOMER

    @Transactional(readOnly = true)
    public List<Account> getAccountsByCustomer(Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        }

        return accountRepository.findByCustomerId(customerId);
    }

    // BLOCK ACCOUNT

    @Transactional
    public Account blockAccount(Long id) {

        Account account = getAccountById(id);
        return blockAccountInternal(account);
    }

    @Transactional
    public Account blockAccount(Long id, Long customerId) {

        Account account = getAccountById(id);
        ensureOwnership(account, customerId);
        return blockAccountInternal(account);
    }

    private Account blockAccountInternal(Account account) {

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException(
                    "A closed account cannot be blocked");
        }

        if (account.getStatus() == AccountStatus.BLOCKED) {
            return account;
        }

        account.setStatus(AccountStatus.BLOCKED);
        Account savedAccount = accountRepository.save(account);

        auditLogService.record(
                currentUsername(),
                AuditAction.ACCOUNT_BLOCKED,
                "Account",
                String.valueOf(savedAccount.getId()),
                "Account blocked");

        return savedAccount;
    }

    // CLOSE ACCOUNT

    @Transactional
    public Account closeAccount(Long id) {

        Account account = getAccountById(id);
        return closeAccountInternal(account);
    }

    @Transactional
    public Account closeAccount(Long id, Long customerId) {

        Account account = getAccountById(id);
        ensureOwnership(account, customerId);
        return closeAccountInternal(account);
    }

    private Account closeAccountInternal(Account account) {

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Account cannot be closed while balance is not zero");
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            return account;
        }

        account.setStatus(AccountStatus.CLOSED);
        Account savedAccount = accountRepository.save(account);

        auditLogService.record(
                currentUsername(),
                AuditAction.ACCOUNT_CLOSED,
                "Account",
                String.valueOf(savedAccount.getId()),
                "Account closed");

        return savedAccount;
    }

    // FUND TRANSFER

    @Transactional
    public TransferResponse transferMoney(
            Long customerId,
            TransferRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Transfer request is required");
        }

        String sourceAccountNumber =
                request.getSourceAccountNumber();

        if (sourceAccountNumber == null
                || sourceAccountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Source account number is required");
        }

        if (request.getBeneficiaryId() == null) {
            throw new IllegalArgumentException(
                    "Beneficiary ID is required");
        }

        BigDecimal amount = request.getAmount();

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        Beneficiary beneficiary =
                beneficiaryRepository.findById(
                                request.getBeneficiaryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Beneficiary not found"));

        if (beneficiary.getCustomer() == null
                || !beneficiary.getCustomer().getId()
                .equals(customerId)) {
            throw new IllegalArgumentException(
                    "Beneficiary does not belong to customer");
        }

        if (beneficiary.getStatus() != BeneficiaryStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Beneficiary is not active");
        }

        String destinationAccountNumber =
                beneficiary.getAccountNumber();

        if (destinationAccountNumber == null
                || destinationAccountNumber.isBlank()) {
            throw new IllegalStateException(
                    "Beneficiary destination account number is missing");
        }

        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts cannot be the same");
        }

        // Lock both account rows before reading balances or updating them.
        List<Account> lockedAccounts =
                accountRepository.findAccountsForUpdate(
                        List.of(
                                sourceAccountNumber,
                                destinationAccountNumber));

        Account sourceAccount = lockedAccounts.stream()
                .filter(account -> sourceAccountNumber.equals(
                        account.getAccountNumber()))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Source account not found"));

        Account destinationAccount = lockedAccounts.stream()
                .filter(account -> destinationAccountNumber.equals(
                        account.getAccountNumber()))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Destination account not found"));

        ensureOwnership(sourceAccount, customerId);

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Source account is not active");
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Destination account is not active");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient account balance");
        }

        sourceAccount.setBalance(
                sourceAccount.getBalance().subtract(amount));

        destinationAccount.setBalance(
                destinationAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        String reference =
                "TXN-" +
                        UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 16)
                                .toUpperCase();

        BankTransaction transaction = new BankTransaction();

        transaction.setTransactionReference(reference);
        transaction.setSourceAccountNumber(
                sourceAccount.getAccountNumber());
        transaction.setDestinationAccountNumber(
                destinationAccount.getAccountNumber());
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        bankTransactionRepository.save(transaction);

        createTransferNotification(
                sourceAccount,
                "Fund transfer successful",
                "Your transfer of " + amount.toPlainString()
                        + " to account "
                        + destinationAccount.getAccountNumber()
                        + " was successful. Reference: " + reference);

        createTransferNotification(
                destinationAccount,
                "Funds received",
                "You have received " + amount.toPlainString()
                        + " from account "
                        + sourceAccount.getAccountNumber()
                        + ". Reference: " + reference);

        auditLogService.record(
                currentUsername(),
                AuditAction.FUND_TRANSFER,
                "BankTransaction",
                reference,
                "Transfer completed for amount "
                        + amount.toPlainString());

        return new TransferResponse(
                reference,
                sourceAccount.getAccountNumber(),
                destinationAccount.getAccountNumber(),
                amount,
                TransactionStatus.SUCCESS.name(),
                "Fund transfer completed successfully");
    }

    // CREATE TRANSFER NOTIFICATION

    private void createTransferNotification(
            Account account,
            String title,
            String message) {

        Long customerId = account.getCustomer().getId();

        userRepository.findByCustomer_Id(customerId)
                .ifPresent(user ->
                        notificationService.createNotification(
                                user.getId(),
                                title,
                                message,
                                NotificationType.TRANSFER));
    }

    // TRANSACTION HISTORY

    @Transactional(readOnly = true)
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

        List<BankTransaction> transactions = new ArrayList<>();
        transactions.addAll(outgoing);
        transactions.addAll(incoming);

        transactions.sort((first, second) ->
                second.getCreatedAt().compareTo(first.getCreatedAt()));

        return transactions;
    }

    // TRANSACTION HISTORY WITH CUSTOMER OWNERSHIP CHECK

    @Transactional(readOnly = true)
    public List<BankTransaction> getTransactionHistoryForCustomer(
            String accountNumber, Long customerId) {

        Account account = getAccountByNumber(accountNumber);
        ensureOwnership(account, customerId);

        return getTransactionHistory(accountNumber);
    }

    // VERIFY ACCOUNT OWNERSHIP

    private void ensureOwnership(Account account, Long customerId) {

        if (account.getCustomer() == null
                || !account.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "Account does not belong to customer");
        }
    }

    // GET CURRENT USERNAME FOR AUDIT

    private String currentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            return "SYSTEM";
        }

        return authentication.getName();
    }

    // GENERATE ACCOUNT NUMBER

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

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}