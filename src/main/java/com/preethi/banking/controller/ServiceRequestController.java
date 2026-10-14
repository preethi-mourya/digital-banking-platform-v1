
package com.preethi.banking.controller;

import com.preethi.banking.entity.ServiceRequest;
import com.preethi.banking.entity.ServiceRequestStatus;
import com.preethi.banking.service.AccountService;
import com.preethi.banking.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final AccountService accountService;

    public ServiceRequestController(
            ServiceRequestService serviceRequestService,
            AccountService accountService) {
        this.serviceRequestService = serviceRequestService;
        this.accountService = accountService;
    }

    @PostMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ServiceRequest createRequest(
            @PathVariable Long customerId,
            @Valid @RequestBody ServiceRequest serviceRequest,
            Principal principal) {

        Long authenticatedCustomerId =
                accountService.getCustomerIdForUsername(
                        principal.getName());

        return serviceRequestService.createRequest(
                authenticatedCustomerId,
                serviceRequest);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<ServiceRequest> getCustomerRequests(
            @PathVariable Long customerId,
            Principal principal) {

        Long authenticatedCustomerId =
                accountService.getCustomerIdForUsername(
                        principal.getName());

        return serviceRequestService.getCustomerRequests(
                authenticatedCustomerId);
    }

    @GetMapping("/customer/{customerId}/{requestId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ServiceRequest getCustomerRequest(
            @PathVariable Long customerId,
            @PathVariable Long requestId,
            Principal principal) {

        Long authenticatedCustomerId =
                accountService.getCustomerIdForUsername(
                        principal.getName());

        return serviceRequestService.getCustomerRequest(
                authenticatedCustomerId,
                requestId);
    }

    @PutMapping("/customer/{customerId}/{requestId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ServiceRequest cancelRequest(
            @PathVariable Long customerId,
            @PathVariable Long requestId,
            Principal principal) {

        Long authenticatedCustomerId =
                accountService.getCustomerIdForUsername(
                        principal.getName());

        return serviceRequestService.cancelRequest(
                authenticatedCustomerId,
                requestId);
    }

    @GetMapping("/employee/status/{status}")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
    public List<ServiceRequest> getRequestsByStatus(
            @PathVariable ServiceRequestStatus status) {

        return serviceRequestService.getRequestsByStatus(status);
    }

    @PutMapping("/employee/{requestId}/start")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
    public ServiceRequest startProcessing(
            @PathVariable Long requestId) {

        return serviceRequestService.startProcessing(requestId);
    }

    @PutMapping("/employee/{requestId}/complete")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
    public ServiceRequest completeRequest(
            @PathVariable Long requestId) {

        return serviceRequestService.completeRequest(requestId);
    }
}
