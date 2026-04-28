package com.example.exception;

// Custom unchecked exception for missing accounts
// Extends RuntimeException — no need to declare in method signature (cleaner API)
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber);
    }

}
