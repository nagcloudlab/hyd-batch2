package com.example.service;

import java.math.BigDecimal;

// ISP — small, focused interface with single method
public interface TransferService {
    void transfer(BigDecimal amount, String fromAccount, String toAccount);
}
