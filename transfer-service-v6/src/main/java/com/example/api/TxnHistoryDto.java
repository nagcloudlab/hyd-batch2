package com.example.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

// DTO for transaction history — flattens the @ManyToOne relationship
// Returns accountNumber as a string instead of nested Account object
@Getter
@AllArgsConstructor
public class TxnHistoryDto {

    private Long id;
    private BigDecimal amount;
    private String transferType;
    private String accountNumber;
    private LocalDateTime timestamp;

}
