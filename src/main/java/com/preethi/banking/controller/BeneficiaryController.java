package com.preethi.banking.controller;

import com.preethi.banking.entity.Beneficiary;
import com.preethi.banking.service.AccountService;
import com.preethi.banking.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final AccountService accountService;

    public BeneficiaryController(
            BeneficiaryService beneficiaryService,
            AccountService accountService) {

        this.beneficiaryService = beneficiaryService;
        this.accountService = accountService;
    }

    private Long requireCustomerId(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException(
                    "Authentication is required");
        }

        return accountService.getCustomerIdForUsername(
                principal.getName());
    }

    private void verifyCustomerPath(
            Long requestedCustomerId,
            Long authenticatedCustomerId) {

        if (!authenticatedCustomerId.equals(requestedCustomerId)) {
            throw new AccessDeniedException(
                    "You are not authorized to access resources "
                            + "for this customer");
        }
    }

    // CREATE BENEFICIARY
    @PostMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Beneficiary createBeneficiary(
            @PathVariable Long customerId,
            @Valid @RequestBody Beneficiary beneficiary,
            Principal principal) {

        Long authenticatedCustomerId =
                requireCustomerId(principal);

        verifyCustomerPath(
                customerId,
                authenticatedCustomerId);

        return beneficiaryService.createBeneficiary(
                authenticatedCustomerId,
                beneficiary);
    }

    // GET BENEFICIARY — owner only
    @GetMapping("/{id}")
    public Beneficiary getBeneficiary(
            @PathVariable Long id,
            Principal principal) {

        Long customerId = requireCustomerId(principal);

        return beneficiaryService.getBeneficiaryForCustomer(
                id,
                customerId);
    }

    // GET CUSTOMER BENEFICIARIES — owner only
    @GetMapping("/customer/{customerId}")
    public List<Beneficiary> getCustomerBeneficiaries(
            @PathVariable Long customerId,
            Principal principal) {

        Long authenticatedCustomerId =
                requireCustomerId(principal);

        verifyCustomerPath(
                customerId,
                authenticatedCustomerId);

        return beneficiaryService.getCustomerBeneficiaries(
                authenticatedCustomerId);
    }

    // DEACTIVATE BENEFICIARY — owner only
    @PutMapping("/{id}/deactivate")
    public Beneficiary deactivateBeneficiary(
            @PathVariable Long id,
            Principal principal) {

        Long customerId = requireCustomerId(principal);

        return beneficiaryService.deactivateBeneficiary(
                id,
                customerId);
    }

    // ACTIVATE BENEFICIARY — owner only
    @PutMapping("/{id}/activate")
    public Beneficiary activateBeneficiary(
            @PathVariable Long id,
            Principal principal) {

        Long customerId = requireCustomerId(principal);

        return beneficiaryService.activateBeneficiary(
                id,
                customerId);
    }
}
