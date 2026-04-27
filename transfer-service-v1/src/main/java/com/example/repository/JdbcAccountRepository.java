package com.example.repository;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.model.Account;

// LSP — Liskov Substitution Principle
// Can replace AccountRepository with JdbcAccountRepository anywhere without breaking behavior
// SRP — Single Responsibility Principle
// Only handles persistence logic using JDBC
// Note: stub implementation — returns hardcoded data. Real DB operations in v3.
public class JdbcAccountRepository implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(JdbcAccountRepository.class);

    public JdbcAccountRepository() {
        logger.info("JdbcAccountRepository instance created.");
    }

    @Override
    public Account findByNumber(String accountNumber) {
        logger.info("Loading account {} from database using JDBC.", accountNumber);
        // Stub — returns hardcoded balance. In v3, this queries Postgres via JdbcTemplate.
        return new Account(accountNumber, new BigDecimal("1000.00"));
    }

    @Override
    public Account save(Account account) {
        logger.info("Saving account {} to database. Balance: ${}.",
                account.getNumber(),
                account.getBalance());
        // Stub — no-op. In v3, this executes UPDATE via JdbcTemplate.
        return account;
    }

}
