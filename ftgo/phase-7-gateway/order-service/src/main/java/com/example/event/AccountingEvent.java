package com.example.event;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class AccountingEvent {
    private String eventType;
    private Long orderId;
    private BigDecimal amount;
    private String restaurantName;
    private String paymentStatus;
    private String stripePaymentId;
    private BigDecimal billTotal;
}
