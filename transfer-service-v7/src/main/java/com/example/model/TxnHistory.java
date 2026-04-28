package com.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// JPA entity — maps to txn_history table
// @ManyToOne — each transaction belongs to one account (FK relationship)
@Getter
@Setter
@Entity
@Table(name = "txn_history")
public class TxnHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BigDecimal for money — never use Double/float (precision issues)
    private BigDecimal amount;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TransferType transferType;

    // @ManyToOne — many transactions can belong to one account
    // @JoinColumn — FK column in txn_history table pointing to accounts.number
    @ManyToOne
    @JoinColumn(name = "account_number", referencedColumnName = "number")
    private Account account;

}
