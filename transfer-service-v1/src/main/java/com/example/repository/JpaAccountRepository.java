package com.example.repository;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.model.Account;

// LSP — Liskov Substitution Principle
// Can substitute JpaAccountRepository wherever AccountRepository is expected
// SRP — Single Responsibility Principle
// Only handles persistence logic using JPA
// Note: stub implementation — returns hardcoded data. Real JPA operations in v5.
public class JpaAccountRepository implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(JpaAccountRepository.class);

    public JpaAccountRepository() {
        logger.info("JpaAccountRepository instance created.");
    }

    @Override
    public Account findByNumber(String accountNumber) {
        logger.info("Loading account {} from database using JPA.", accountNumber);
        // Stub — returns hardcoded balance. In v5, this queries via EntityManager.
        return new Account(accountNumber, new BigDecimal("1000.00"));
    }

    @Override
    public Account save(Account account) {
        logger.info("Saving account {} to database. Balance: ${}.",
                account.getNumber(),
                account.getBalance());
        // Stub — no-op. In v5, this persists via EntityManager.
        return account;
    }

}
