package com.example.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts", schema = "public")
public class Account {

    @Id
    private String number;
    // Mutable — balance changes during transfers (debit/credit operations)
    private BigDecimal balance;

    public Account() {
        // Default constructor for JPA
    }

    // Guard clauses — validate inputs at construction time, fail fast
    public Account(String number, BigDecimal balance) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be null or blank");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        this.number = number;
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "number='" + number + '\'' +
                ", balance=" + balance +
                '}';
    }

}
