package com.example.repository;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.model.Account;

// LSP — Can replace AccountRepository with JdbcAccountRepository anywhere without breaking behavior
// SRP — Only handles persistence logic using JDBC
// @Repository — specialization of @Component, indicates a data access component
// @Scope("singleton") — default scope, one instance per container (shown for demo, can be omitted)
@Qualifier("jdbc")
@Repository("jdbcAccountRepository")
@Scope("singleton")
public class JdbcAccountRepository implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(JdbcAccountRepository.class);

    private JdbcTemplate jdbcTemplate;

    // @Autowired
    public JdbcAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("JdbcAccountRepository initialized with JdbcTemplate: {}", jdbcTemplate);
    }

    @Override
    public Account findByNumber(String accountNumber) {
        logger.info("Loading account {} from database using JDBC.", accountNumber);
        String sql = "SELECT * FROM accounts WHERE number = ?";
        return jdbcTemplate.queryForObject(sql, new Object[] { accountNumber }, (rs, rowNum) -> {
            BigDecimal balance = rs.getBigDecimal("balance");
            return new Account(accountNumber, balance);
        });
    }

    @Override
    public Account save(Account account) {
        logger.info("Saving account {} to database. Balance: ${}.",
                account.getNumber(),
                account.getBalance());
        String sql = "UPDATE accounts SET balance = ? WHERE number = ?";
        jdbcTemplate.update(sql, account.getBalance(), account.getNumber());
        return account;
    }

}
