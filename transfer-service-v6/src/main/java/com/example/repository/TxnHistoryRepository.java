package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.TxnHistory;

// Spring Data JPA — auto-generates query from method name
// findByAccountNumber → SELECT * FROM txn_history WHERE account_number = ?
public interface TxnHistoryRepository extends JpaRepository<TxnHistory, Long> {

    // Derived query method — Spring Data parses the method name and generates SQL
    List<TxnHistory> findByAccountNumber(String accountNumber);

}
