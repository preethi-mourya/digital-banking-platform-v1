package com.preethi.banking.service;

import com.preethi.banking.entity.Beneficiary;
import com.preethi.banking.entity.BeneficiaryStatus;
import com.preethi.banking.entity.Customer;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.BeneficiaryRepository;
import com.preethi.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            CustomerRepository customerRepository) {

        this.beneficiaryRepository = beneficiaryRepository;
        this.customerRepository = customerRepository;
    }

    public Beneficiary createBeneficiary(
            Long customerId,
            Beneficiary beneficiary) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: "
                                        + customerId));

        boolean alreadyExists =
                beneficiaryRepository
                        .existsByCustomerIdAndAccountNumber(
                                customerId,
                                beneficiary.getAccountNumber());

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "Beneficiary with this account number already exists");
        }

        beneficiary.setCustomer(customer);
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);

        return beneficiaryRepository.save(beneficiary);
    }

    public Beneficiary getBeneficiary(Long id) {

        return beneficiaryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Beneficiary not found with id: "
                                        + id));
    }

    public List<Beneficiary> getCustomerBeneficiaries(
            Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        }

        return beneficiaryRepository.findByCustomerId(customerId);
    }

    public Beneficiary deactivateBeneficiary(Long id) {

        Beneficiary beneficiary = getBeneficiary(id);

        beneficiary.setStatus(BeneficiaryStatus.INACTIVE);

        return beneficiaryRepository.save(beneficiary);
    }

    public Beneficiary activateBeneficiary(Long id) {

        Beneficiary beneficiary = getBeneficiary(id);

        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);

        return beneficiaryRepository.save(beneficiary);
    }
}