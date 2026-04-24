package com.example.repository;

import org.slf4j.Logger;

import com.example.model.Account;

public class JpaAccountRepository implements AccountRepository {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");

    public JpaAccountRepository() {
        logger.info("JpaAccountRepository instance created.");
    }

    @Override
    public Account findByNumber(String accountNumber) {
        // Implementation to load account from the database using JPA
        return new Account(accountNumber, 1000.00);
    }

    @Override
    public Account save(Account account) {
        // Implementation to update account balance in the database using JPA
        // For demonstration, we simply return the updated account
        logger.info("Saving account with account number {} to the database. New balance: ${}.",
                account.getNumber(),
                account.getBalance());
        return account;
    }

}
