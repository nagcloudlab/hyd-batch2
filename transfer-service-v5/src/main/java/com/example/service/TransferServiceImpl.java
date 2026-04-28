package com.example.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.exception.AccountNotFoundException;
import com.example.exception.InsufficientFundsException;
import com.example.model.Account;
import com.example.repository.AccountRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

// SRP — only handles transfer business logic, delegates persistence to repository
// DIP — depends on AccountRepository abstraction, not on concrete implementations
// @Service — specialization of @Component, indicates a business logic component
@Service("transferService")
public class TransferServiceImpl implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    // 'final' — dependency cannot be reassigned after construction (safe DI
    // practice)
    private final AccountRepository accountRepository;

    @Value("${npci.transfer.limit:10000.00}") // Inject from properties, default to 10000.00 if not set
    private double transferLimit; // Example of a configurable property with a default value

    // Constructor Injection — Spring auto-detects single constructor, @Autowired
    // optional
    public TransferServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        logger.info("TransferServiceImpl initialized with {} repository.",
                accountRepository.getClass().getSimpleName());
    }

    // @PostConstruct — called after DI is complete, use for initialization logic
    @PostConstruct
    public void init() {
        logger.info("TransferServiceImpl bean is initialized and ready to use.");
    }

    // @PreDestroy — called before bean is removed, use for cleanup/resource release
    @PreDestroy
    public void destroy() {
        logger.info("TransferServiceImpl bean is being destroyed.");
    }

    // ACID Properties:
    // Atomicity — all steps succeed or all fail (rollback on exception)
    // Consistency — ensures data integrity (e.g., no negative balances)
    // Isolation — concurrent transactions do not interfere (READ_COMMITTED)
    // Durability — committed data survives DB restart
    //
    // How it works: @Transactional -> TransactionInterceptor (AOP) ->
    // PlatformTransactionManager
    @Transactional(
            // rollbackFor — which exceptions trigger rollback (RuntimeException by default
            // anyway)
            rollbackFor = RuntimeException.class,
            // noRollbackFor — InsufficientFundsException is a business rule violation, not
            // a data error;
            // no DB changes were made yet, so no rollback needed
            noRollbackFor = InsufficientFundsException.class,
            // READ_COMMITTED — prevents dirty reads; each transaction sees only committed
            // data
            // Other options: READ_UNCOMMITTED, REPEATABLE_READ, SERIALIZABLE
            isolation = Isolation.READ_COMMITTED)
    @Override
    public void transfer(BigDecimal amount, String fromAccountNumber, String toAccountNumber) {

        System.out.println("--------------------------------");
        System.out.println("Transfer Limit: $" + transferLimit);
        System.out.println("--------------------------------");

        // Validate inputs at boundary — fail fast before any business logic runs
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (fromAccountNumber == null || fromAccountNumber.isBlank()) {
            throw new IllegalArgumentException("From account number is required");
        }
        if (toAccountNumber == null || toAccountNumber.isBlank()) {
            throw new IllegalArgumentException("To account number is required");
        }
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        logger.info("Initiating transfer of ${} from account {} to account {}.",
                amount, fromAccountNumber, toAccountNumber);

        // step-1: Load 'from' account
        Account fromAccount = accountRepository.findById(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(fromAccountNumber));

        // Fail fast — use compareTo() for BigDecimal, never == or equals
        // (BigDecimal("1.0").equals(BigDecimal("1.00")) returns false due to scale
        // difference)
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromAccountNumber, fromAccount.getBalance(), amount);
        }

        // step-2: Load 'to' account
        Account toAccount = accountRepository.findById(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(toAccountNumber));

        // step-3: Debit 'from' account
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        logger.info("Debited ${} from account {}. New balance: ${}.",
                amount, fromAccountNumber, fromAccount.getBalance());

        // step-4: Credit 'to' account
        toAccount.setBalance(toAccount.getBalance().add(amount));
        logger.info("Credited ${} to account {}. New balance: ${}.",
                amount, toAccountNumber, toAccount.getBalance());

        // step-5: Save updated accounts
        accountRepository.save(fromAccount);

        // Atomicity demo — set to true to simulate failure between debit and credit
        // Transaction rolls back: fromAccount debit is undone, toAccount credit never
        // happens
        boolean simulateError = false;
        if (simulateError) {
            logger.warn("Simulating error after debit but before credit save.");
            throw new RuntimeException("Simulated error to test transaction rollback");
        }

        accountRepository.save(toAccount);

        logger.info("Transfer of ${} from account {} to account {} completed successfully.",
                amount, fromAccountNumber, toAccountNumber);
    }

}
