package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.exception.AccountNotFoundException;
import com.example.exception.InsufficientFundsException;
import com.example.model.Account;
import com.example.model.TransferType;
import com.example.model.TxnHistory;
import com.example.repository.AccountRepository;
import com.example.repository.TxnHistoryRepository;

// SRP — only handles transfer business logic, delegates persistence to repository
// DIP — depends on AccountRepository abstraction, not on concrete implementations
// @Service — specialization of @Component, indicates a business logic component
@Service("transferService")
public class TransferServiceImpl implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TxnHistoryRepository txnHistoryRepository;

    // @Value — injects from application.properties with default fallback
    @Value("${npci.transfer.limit:10000.00}")
    private BigDecimal transferLimit;

    // Constructor Injection — Spring auto-detects single constructor, @Autowired optional
    public TransferServiceImpl(AccountRepository accountRepository,
                               TxnHistoryRepository txnHistoryRepository) {
        this.accountRepository = accountRepository;
        this.txnHistoryRepository = txnHistoryRepository;
    }

    @Transactional(
        rollbackFor = RuntimeException.class,
        noRollbackFor = InsufficientFundsException.class,
        isolation = Isolation.READ_COMMITTED
    )
    @Override
    public void transfer(BigDecimal amount, String fromAccountNumber, String toAccountNumber) {

        logger.info("Transfer limit: ${}", transferLimit);

        // Validate inputs at boundary — fail fast before any business logic runs
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (amount.compareTo(transferLimit) > 0) {
            throw new IllegalArgumentException("Transfer amount exceeds limit of $" + transferLimit);
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

        // step-1: Load 'from' account (JPA findById returns Optional)
        Account fromAccount = accountRepository.findById(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(fromAccountNumber));

        // Fail fast — use compareTo() for BigDecimal, never == or equals
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

        // step-5: Save updated accounts (JPA dirty checking would auto-save, but explicit is clearer)
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // step-6: Record transaction history
        TxnHistory debitTxn = new TxnHistory();
        debitTxn.setAmount(amount);
        debitTxn.setTimestamp(LocalDateTime.now());
        debitTxn.setTransferType(TransferType.DEBIT);
        debitTxn.setAccount(fromAccount);
        txnHistoryRepository.save(debitTxn);

        TxnHistory creditTxn = new TxnHistory();
        creditTxn.setAmount(amount);
        creditTxn.setTimestamp(LocalDateTime.now());
        creditTxn.setTransferType(TransferType.CREDIT);
        creditTxn.setAccount(toAccount);
        txnHistoryRepository.save(creditTxn);

        logger.info("Transfer of ${} from account {} to account {} completed successfully.",
                amount, fromAccountNumber, toAccountNumber);
    }

}
