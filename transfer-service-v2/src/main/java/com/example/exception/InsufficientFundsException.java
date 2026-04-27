package com.example.exception;

import java.math.BigDecimal;

// Custom unchecked exception for insufficient balance during transfer
// Extends RuntimeException — no need to declare in method signature (cleaner API)
// Includes account number, available, and requested amounts for clear diagnostics
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountNumber, BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient funds in account %s. Available: $%s, Requested: $%s",
                accountNumber, available, requested));
    }

}
