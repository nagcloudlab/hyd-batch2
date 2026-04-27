package com.example.repository;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.example.model.Account;

// LSP — Can substitute JpaAccountRepository wherever AccountRepository is expected
// SRP — Only handles persistence logic using JPA
// @Primary — when multiple beans qualify, Spring picks this one by default
// @Primary
@Qualifier("jpa")
@Repository("jpaAccountRepository")
@Profile("prod")
public class JpaAccountRepository implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(JpaAccountRepository.class);

    public JpaAccountRepository() {
        logger.info("JpaAccountRepository instance created.");
    }

    @Override
    public Account findByNumber(String accountNumber) {
        logger.info("Loading account {} from database using JPA.", accountNumber);
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
