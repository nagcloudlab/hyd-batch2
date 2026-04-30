package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal tax;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private LocalDateTime generatedTime;
}
