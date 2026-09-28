package com.preethi.banking.controller;

import com.preethi.banking.entity.Beneficiary;
import com.preethi.banking.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(
            BeneficiaryService beneficiaryService) {

        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Beneficiary createBeneficiary(
            @PathVariable Long customerId,
            @Valid @RequestBody Beneficiary beneficiary) {

        return beneficiaryService.createBeneficiary(
                customerId,
                beneficiary);
    }

    @GetMapping("/{id}")
    public Beneficiary getBeneficiary(
            @PathVariable Long id) {

        return beneficiaryService.getBeneficiary(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Beneficiary> getCustomerBeneficiaries(
            @PathVariable Long customerId) {

        return beneficiaryService.getCustomerBeneficiaries(
                customerId);
    }

    @PutMapping("/{id}/deactivate")
    public Beneficiary deactivateBeneficiary(
            @PathVariable Long id) {

        return beneficiaryService.deactivateBeneficiary(id);
    }

    @PutMapping("/{id}/activate")
    public Beneficiary activateBeneficiary(
            @PathVariable Long id) {

        return beneficiaryService.activateBeneficiary(id);
    }
}