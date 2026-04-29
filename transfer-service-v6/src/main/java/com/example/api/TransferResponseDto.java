package com.example.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Immutable response DTO — all fields set via constructor, no setters
@Getter
@AllArgsConstructor
public class TransferResponseDto {

    private String status;
    private BigDecimal amount;
    private String fromAccount;
    private String toAccount;
    private LocalDateTime timestamp;

}
