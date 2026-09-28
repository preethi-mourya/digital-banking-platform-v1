package com.preethi.banking.service;

import com.preethi.banking.entity.Customer;
import com.preethi.banking.entity.ServiceRequest;
import com.preethi.banking.entity.ServiceRequestStatus;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.CustomerRepository;
import com.preethi.banking.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerRepository customerRepository;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository,
            CustomerRepository customerRepository) {

        this.serviceRequestRepository = serviceRequestRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public ServiceRequest createRequest(
            Long customerId,
            ServiceRequest serviceRequest) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId));

        serviceRequest.setRequestReference(generateRequestReference());

        serviceRequest.setStatus(ServiceRequestStatus.PENDING);

        LocalDateTime now = LocalDateTime.now();
        serviceRequest.setCreatedAt(now);
        serviceRequest.setUpdatedAt(now);

        serviceRequest.setCustomer(customer);

        return serviceRequestRepository.save(serviceRequest);
    }

    public List<ServiceRequest> getCustomerRequests(
            Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        }

        return serviceRequestRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public ServiceRequest getCustomerRequest(
            Long customerId,
            Long requestId) {

        return serviceRequestRepository
                .findByIdAndCustomerId(requestId, customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Service request not found for customer"));
    }

    @Transactional
    public ServiceRequest cancelRequest(
            Long customerId,
            Long requestId) {

        ServiceRequest request =
                getCustomerRequest(customerId, requestId);

        if (request.getStatus() != ServiceRequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending service requests can be cancelled");
        }

        request.setStatus(ServiceRequestStatus.CANCELLED);
        request.setUpdatedAt(LocalDateTime.now());

        return serviceRequestRepository.save(request);
    }

    public List<ServiceRequest> getRequestsByStatus(
            ServiceRequestStatus status) {

        return serviceRequestRepository
                .findByStatusOrderByCreatedAtAsc(status);
    }

    @Transactional
    public ServiceRequest startProcessing(Long requestId) {

        ServiceRequest request =
                serviceRequestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service request not found with id: "
                                                + requestId));

        if (request.getStatus()
                != ServiceRequestStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending requests can be moved to in-progress");
        }

        request.setStatus(ServiceRequestStatus.IN_PROGRESS);
        request.setUpdatedAt(LocalDateTime.now());

        return serviceRequestRepository.save(request);
    }

    @Transactional
    public ServiceRequest completeRequest(Long requestId) {

        ServiceRequest request =
                serviceRequestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service request not found with id: "
                                                + requestId));

        if (request.getStatus()
                != ServiceRequestStatus.IN_PROGRESS) {

            throw new IllegalStateException(
                    "Only in-progress requests can be completed");
        }

        request.setStatus(ServiceRequestStatus.COMPLETED);
        request.setUpdatedAt(LocalDateTime.now());

        return serviceRequestRepository.save(request);
    }

    private String generateRequestReference() {

        return "SR-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();
    }
}