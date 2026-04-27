package com.example.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.exception.AccountNotFoundException;
import com.example.exception.InsufficientFundsException;
import com.example.repository.AccountRepository;

// SRP — only handles transfer business logic, delegates persistence to repository
// DIP — depends on AccountRepository abstraction, not on JdbcAccountRepository or JpaAccountRepository
public class TransferServiceImpl implements TransferService {

    // 'final' — dependency cannot be reassigned after construction (safe DI practice)
    private final AccountRepository accountRepository;

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    // Constructor Injection — dependency is provided from outside (IoC)
    // This class does NOT create or lookup its own dependency
    public TransferServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        logger.info("TransferServiceImpl initialized with {} repository.",
                accountRepository.getClass().getSimpleName());
    }

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

        // Problem 1: tight coupling — creating dependency directly
        // JdbcAccountRepository accountRepository = new JdbcAccountRepository();

        // Problem 2: still coupled — dependent is looking up its own dependency from factory
        // AccountRepository accountRepository = AccountRepositoryFactory.createAccountRepository("jpa");

        // Solution: don't create, don't lookup — always inject from outside (IoC)

        // step-1: Load 'from' account
        var fromAccount = accountRepository.findByNumber(fromAccountNumber);
        if (fromAccount == null) {
            throw new AccountNotFoundException(fromAccountNumber);
        }

        // Fail fast with custom exception. Use compareTo() for BigDecimal, never == or equals
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
        accountRepository.save(toAccount);

        logger.info("Transfer of ${} from account {} to account {} completed successfully.",
                amount, fromAccountNumber, toAccountNumber);
    }

}
