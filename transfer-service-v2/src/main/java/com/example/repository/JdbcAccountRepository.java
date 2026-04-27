package com.example.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import com.example.model.Account;

// LSP — Can replace AccountRepository with JdbcAccountRepository anywhere without breaking behavior
// SRP — Only handles persistence logic using JDBC
// @Repository — specialization of @Component, indicates a data access component
@Qualifier("jdbc")
@Repository("jdbcAccountRepository")
@Profile("dev")
@Scope("singleton") // Default scope, can be omitted , prototype, request,session
public class JdbcAccountRepository implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(JdbcAccountRepository.class);

    // DataSource is injected by Spring container — no manual creation
    private final DataSource dataSource;

    @Autowired
    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        logger.info("JdbcAccountRepository initialized with DataSource: {}", dataSource);
    }

    @Override
    public Account findByNumber(String accountNumber) {
        logger.info("Loading account {} from database using JDBC.", accountNumber);
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
