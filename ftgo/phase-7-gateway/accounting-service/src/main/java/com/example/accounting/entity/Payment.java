package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String stripePaymentId;
    private LocalDateTime paymentTime;
}
