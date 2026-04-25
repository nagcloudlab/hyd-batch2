package com.example.repository;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.model.Account;

// LSP — Liskov Substitution Principle
// Can replace AccountRepository with JdbcAccountRepository anywhere without breaking behavior
// SRP — Single Responsibility Principle
// Only handles persistence logic using JDBC
public class JdbcAccountRepository implements AccountRepository {

    // Use ClassName.class instead of string literals — enables proper logger hierarchy and filtering
    private static final Logger logger = LoggerFactory.getLogger(JdbcAccountRepository.class);

    public JdbcAccountRepository() {
        logger.info("JdbcAccountRepository instance created.");
    }

    // @Override — always annotate, catches errors at compile time if interface changes
    @Override
    public Account findByNumber(String accountNumber) {
        logger.info("Loading account {} from database using JDBC.", accountNumber);
        return new Account(accountNumber, new BigDecimal("1000.00"));
    }

    @Override
    public Account save(Account account) {
        logger.info("Saving account {} to database. Balance: ${}.",
                account.getNumber(),
                account.getBalance());
        return account;
    }

}
