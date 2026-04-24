package com.example.repository;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;

import com.example.model.Account;

/**
 * author: team-1
 */

public class JdbcAccountRepository implements AccountRepository {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("txr-service");
    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        logger.info("JdbcAccountRepository initialized with DataSource: {}", dataSource);
    }

    public Account findByNumber(String accountNumber) {
        logger.info("Loading account with account number {} from the database.", accountNumber);
        // Implementation to load account from the database using JDBC
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Error obtaining database connection: {}", e.getMessage());
            throw new RuntimeException("Database connection error", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("Error closing database connection: {}", e.getMessage());
                }
            }
        }
        return new Account(accountNumber, 1000.00);
    }

    public Account save(Account account) {
        // Implementation to update account balance in the database using JDBC
        // For demonstration, we simply return the updated account
        logger.info("Saving account with account number {} to the database. New balance: ${}.",
                account.getNumber(),
                account.getBalance());
        return account;
    }

}
