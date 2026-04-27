package com.example.repository;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.model.Account;

// Real database persistence using Spring JdbcTemplate
// Compare with v2 JdbcAccountRepository — which was a stub returning hardcoded data
// JdbcTemplate handles: connection management, statement creation, exception translation
@Repository("jdbcAccountRepository")
public class JdbcAccountRepository implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(JdbcAccountRepository.class);

    private final JdbcTemplate jdbcTemplate;

    // Constructor Injection — Spring auto-detects single constructor
    public JdbcAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("JdbcAccountRepository initialized with JdbcTemplate.");
    }

    @Override
    public Account findByNumber(String accountNumber) {
        logger.info("Loading account {} from database.", accountNumber);
        String sql = "SELECT * FROM accounts WHERE number = ?";
        try {
            // RowMapper lambda: maps each ResultSet row to an Account object
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                BigDecimal balance = rs.getBigDecimal("balance");
                return new Account(accountNumber, balance);
            }, accountNumber);
        } catch (EmptyResultDataAccessException e) {
            // queryForObject throws EmptyResultDataAccessException when no row found (never returns null)
            return null;
        }
    }

    @Override
    public Account save(Account account) {
        logger.info("Saving account {} to database. Balance: ${}.",
                account.getNumber(), account.getBalance());
        // UPDATE only — assumes account already exists in the database
        String sql = "UPDATE accounts SET balance = ? WHERE number = ?";
        jdbcTemplate.update(sql, account.getBalance(), account.getNumber());
        return account;
    }

}
