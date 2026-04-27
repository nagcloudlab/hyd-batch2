package com.example.service;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.exception.AccountNotFoundException;
import com.example.exception.InsufficientFundsException;
import com.example.repository.AccountRepository;

// SRP — only handles transfer business logic, delegates persistence to repository
// DIP — depends on AccountRepository abstraction, not on concrete implementations
// @Service — specialization of @Component, indicates a business logic component
@Service("transferService")
public class TransferServiceImpl implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    // 'final' — dependency cannot be reassigned after construction (safe DI
    // practice)
    private final AccountRepository accountRepository;

    // Constructor Injection — Spring auto-detects single constructor, @Autowired
    // optional
    // @Qualifier("jdbc") — picks JdbcAccountRepository when multiple beans
    // implement AccountRepository
    public TransferServiceImpl(/* @Qualifier("jdbc") */ AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        logger.info("TransferServiceImpl initialized with {} repository.",
                accountRepository.getClass().getSimpleName());
    }

    // Setter DI — alternative to constructor injection (uncomment to demo)
    // @Autowired(required = true)
    // public void setAccountRepository(AccountRepository accountRepository) {
    // this.accountRepository = accountRepository;
    // }

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

    // ACID
    // Atomicity — all steps succeed or all fail (rollback on exception)
    // Consistency — ensures data integrity (e.g., no negative balances)
    // Isolation — concurrent transactions do not interfere (handled by DB)

    // -> TransactionalAspect -> TransactionInterceptor ->TransactionManager
    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = InsufficientFundsException.class, isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    @Override
    public void transfer(BigDecimal amount, String fromAccountNumber, String toAccountNumber) {

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
        var fromAccount = accountRepository.findByNumber(fromAccountNumber);
        if (fromAccount == null) {
            throw new AccountNotFoundException(fromAccountNumber);
        }

        // Fail fast with custom exception. Use compareTo() for BigDecimal, never == or
        // equals
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromAccountNumber, fromAccount.getBalance(), amount);
        }

        // step-2: Load 'to' account
        var toAccount = accountRepository.findByNumber(toAccountNumber);
        if (toAccount == null) {
            throw new AccountNotFoundException(toAccountNumber);
        }

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

        boolean simulateError = false; // Set to true to test rollback behavior
        if (simulateError) {
            logger.warn("Simulating error after debiting 'from' account but before saving 'to' account.");
            throw new RuntimeException("Simulated error to test transaction rollback");
        }

        accountRepository.save(toAccount);

        logger.info("Transfer of ${} from account {} to account {} completed successfully.",
                amount, fromAccountNumber, toAccountNumber);

    }

}
