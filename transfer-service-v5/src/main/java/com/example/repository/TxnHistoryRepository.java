package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.TxnHistory;

public interface TxnHistoryRepository extends JpaRepository<TxnHistory, Long> {

}
