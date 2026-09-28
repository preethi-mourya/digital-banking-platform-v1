package com.preethi.banking.repository;

import com.preethi.banking.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    Optional<BankTransaction> findByTransactionReference(
            String transactionReference);

    List<BankTransaction> findBySourceAccountNumberOrderByCreatedAtDesc(
            String accountNumber);

    List<BankTransaction> findByDestinationAccountNumberOrderByCreatedAtDesc(
            String accountNumber);
}