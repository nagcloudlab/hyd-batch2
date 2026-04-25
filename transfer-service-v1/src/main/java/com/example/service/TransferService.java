package com.example.service;

import java.math.BigDecimal;

// ISP — small, focused interface with single method
// SRP — only defines the transfer operation contract
public interface TransferService {
    void transfer(BigDecimal amount, String fromAccount, String toAccount);
}
