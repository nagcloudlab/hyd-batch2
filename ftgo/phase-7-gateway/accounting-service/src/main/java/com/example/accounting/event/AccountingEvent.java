package com.example.accounting.event;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class AccountingEvent {
    private String eventType;       // PAYMENT_REQUESTED, PAYMENT_COMPLETED, PAYMENT_FAILED
    private Long orderId;
    private BigDecimal amount;
    private String restaurantName;
    private String paymentStatus;
    private String stripePaymentId;
    private BigDecimal billTotal;
}
