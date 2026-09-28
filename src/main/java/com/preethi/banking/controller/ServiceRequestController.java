package com.preethi.banking.controller;

import com.preethi.banking.entity.ServiceRequest;
import com.preethi.banking.entity.ServiceRequestStatus;
import com.preethi.banking.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestController(
            ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    // Customer creates a service request
    @PostMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ServiceRequest createRequest(
            @PathVariable Long customerId,
            @Valid @RequestBody ServiceRequest serviceRequest) {

        return serviceRequestService.createRequest(
                customerId,
                serviceRequest);
    }

    // Customer views all their service requests
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<ServiceRequest> getCustomerRequests(
            @PathVariable Long customerId) {

        return serviceRequestService
                .getCustomerRequests(customerId);
    }

    // Customer views one of their requests
    @GetMapping("/customer/{customerId}/{requestId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ServiceRequest getCustomerRequest(
            @PathVariable Long customerId,
            @PathVariable Long requestId) {

        return serviceRequestService.getCustomerRequest(
                customerId,
                requestId);
    }

    // Customer cancels an eligible request
    @PutMapping("/customer/{customerId}/{requestId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ServiceRequest cancelRequest(
            @PathVariable Long customerId,
            @PathVariable Long requestId) {

        return serviceRequestService.cancelRequest(
                customerId,
                requestId);
    }

    // Employee views requests by status
    @GetMapping("/employee/status/{status}")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
    public List<ServiceRequest> getRequestsByStatus(
            @PathVariable ServiceRequestStatus status) {

        return serviceRequestService
                .getRequestsByStatus(status);
    }

    // Employee starts processing a request
    @PutMapping("/employee/{requestId}/start")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
    public ServiceRequest startProcessing(
            @PathVariable Long requestId) {

        return serviceRequestService
                .startProcessing(requestId);
    }

    // Employee completes a request
    @PutMapping("/employee/{requestId}/complete")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
    public ServiceRequest completeRequest(
            @PathVariable Long requestId) {

        return serviceRequestService
                .completeRequest(requestId);
    }
}