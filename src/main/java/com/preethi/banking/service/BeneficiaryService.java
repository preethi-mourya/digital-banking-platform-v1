package com.preethi.banking.service;

import com.preethi.banking.entity.Account;
import com.preethi.banking.entity.AuditAction;
import com.preethi.banking.entity.Beneficiary;
import com.preethi.banking.entity.BeneficiaryStatus;
import com.preethi.banking.entity.Customer;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.AccountRepository;
import com.preethi.banking.repository.BeneficiaryRepository;
import com.preethi.banking.repository.CustomerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            AuditLogService auditLogService) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.auditLogService = auditLogService;
    }

    // CREATE BENEFICIARY

    @Transactional
    public Beneficiary createBeneficiary(
            Long customerId, Beneficiary beneficiary) {

        if (beneficiary == null) {
            throw new IllegalArgumentException(
                    "Beneficiary details are required");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId));

        String accountNumber = beneficiary.getAccountNumber();

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Beneficiary account number is required");
        }

        accountNumber = accountNumber.trim();

        Account destinationAccount =
                accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Destination account not found"));

        if (destinationAccount.getStatus()
                != com.preethi.banking.entity.AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Destination account is not active");
        }

        boolean alreadyExists =
                beneficiaryRepository
                        .existsByCustomerIdAndAccountNumber(
                                customerId,
                                accountNumber);

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "This beneficiary account already exists for the customer");
        }

        beneficiary.setAccountNumber(accountNumber);
        beneficiary.setCustomer(customer);
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);

        Beneficiary savedBeneficiary =
                beneficiaryRepository.save(beneficiary);

        auditLogService.record(
                currentUsername(),
                AuditAction.BENEFICIARY_CREATED,
                "Beneficiary",
                String.valueOf(savedBeneficiary.getId()),
                "Beneficiary created for customer ID " + customerId);

        return savedBeneficiary;
    }

    // GET BENEFICIARY BY ID

    @Transactional(readOnly = true)
    public Beneficiary getBeneficiary(Long id) {

        return beneficiaryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Beneficiary not found with id: " + id));
    }

    // GET BENEFICIARY WITH CUSTOMER OWNERSHIP CHECK

    @Transactional(readOnly = true)
    public Beneficiary getBeneficiaryForCustomer(
            Long id, Long customerId) {

        Beneficiary beneficiary = getBeneficiary(id);
        ensureOwnership(beneficiary, customerId);
        return beneficiary;
    }

    // GET ALL BENEFICIARIES OF CUSTOMER

    @Transactional(readOnly = true)
    public List<Beneficiary> getCustomerBeneficiaries(Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        }

        return beneficiaryRepository.findByCustomerId(customerId);
    }

    // DEACTIVATE BENEFICIARY

    @Transactional
    public Beneficiary deactivateBeneficiary(Long id) {

        Beneficiary beneficiary = getBeneficiary(id);
        return deactivateBeneficiaryInternal(beneficiary);
    }

    @Transactional
    public Beneficiary deactivateBeneficiary(
            Long id, Long customerId) {

        Beneficiary beneficiary = getBeneficiary(id);
        ensureOwnership(beneficiary, customerId);

        return deactivateBeneficiaryInternal(beneficiary);
    }

    private Beneficiary deactivateBeneficiaryInternal(
            Beneficiary beneficiary) {

        if (beneficiary.getStatus() == BeneficiaryStatus.INACTIVE) {
            return beneficiary;
        }

        beneficiary.setStatus(BeneficiaryStatus.INACTIVE);

        Beneficiary savedBeneficiary =
                beneficiaryRepository.save(beneficiary);

        auditLogService.record(
                currentUsername(),
                AuditAction.BENEFICIARY_DEACTIVATED,
                "Beneficiary",
                String.valueOf(savedBeneficiary.getId()),
                "Beneficiary deactivated");

        return savedBeneficiary;
    }

    // ACTIVATE BENEFICIARY

    @Transactional
    public Beneficiary activateBeneficiary(Long id) {

        Beneficiary beneficiary = getBeneficiary(id);
        return activateBeneficiaryInternal(beneficiary);
    }

    @Transactional
    public Beneficiary activateBeneficiary(
            Long id, Long customerId) {

        Beneficiary beneficiary = getBeneficiary(id);
        ensureOwnership(beneficiary, customerId);

        return activateBeneficiaryInternal(beneficiary);
    }

    private Beneficiary activateBeneficiaryInternal(
            Beneficiary beneficiary) {

        if (beneficiary.getStatus() == BeneficiaryStatus.ACTIVE) {
            return beneficiary;
        }

        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);

        Beneficiary savedBeneficiary =
                beneficiaryRepository.save(beneficiary);

        auditLogService.record(
                currentUsername(),
                AuditAction.BENEFICIARY_ACTIVATED,
                "Beneficiary",
                String.valueOf(savedBeneficiary.getId()),
                "Beneficiary activated");

        return savedBeneficiary;
    }

    // VERIFY BENEFICIARY OWNERSHIP

    private void ensureOwnership(
            Beneficiary beneficiary, Long customerId) {

        if (beneficiary.getCustomer() == null
                || !beneficiary.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "Beneficiary does not belong to customer");
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
}
