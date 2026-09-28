package com.preethi.banking.dto;

import java.math.BigDecimal;

public class TransferResponse {

    private String transactionReference;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String status;
    private String message;

    public TransferResponse() {
    }

    public TransferResponse(
            String transactionReference,
            String sourceAccountNumber,
            String destinationAccountNumber,
            BigDecimal amount,
            String status,
            String message) {

        this.transactionReference = transactionReference;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.status = status;
        this.message = message;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}