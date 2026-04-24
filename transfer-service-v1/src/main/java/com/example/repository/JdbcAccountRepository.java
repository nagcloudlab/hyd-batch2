package com.example.repository;

import org.slf4j.Logger;

import com.example.model.Account;

/**
 * author: team-1
 */

public class JdbcAccountRepository {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");

    public JdbcAccountRepository() {
        logger.info("JdbcAccountRepository instance created.");
    }

    public Account findByAccountNumber(String accountNumber) {
        logger.info("Loading account with account number {} from the database.", accountNumber);
        // Implementation to load account from the database using JDBC
        return new Account(accountNumber, 1000.00);
    }

    public Account saveAccount(Account account) {
        // Implementation to update account balance in the database using JDBC
        // For demonstration, we simply return the updated account
        logger.info("Saving account with account number {} to the database. New balance: ${}.",
                account.getNumber(),
                account.getBalance());
        return account;
    }

}
