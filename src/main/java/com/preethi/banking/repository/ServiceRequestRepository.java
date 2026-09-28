package com.preethi.banking.repository;

import com.preethi.banking.entity.ServiceRequest;
import com.preethi.banking.entity.ServiceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long> {

    Optional<ServiceRequest> findByRequestReference(
            String requestReference);

    List<ServiceRequest> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId);

    Optional<ServiceRequest> findByIdAndCustomerId(
            Long id,
            Long customerId);

    List<ServiceRequest> findByStatusOrderByCreatedAtAsc(
            ServiceRequestStatus status);
}