package com.example.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "txn_history")
public class TxnHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    private Date timestamp;
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private TransferType transferType;
    @ManyToOne
    @JoinColumn(name = "account_number", referencedColumnName = "number")
    private Account account;

}
