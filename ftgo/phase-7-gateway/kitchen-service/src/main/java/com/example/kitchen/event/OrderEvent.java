package com.example.kitchen.event;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class OrderEvent {
    private String eventType;       // ORDER_READY_FOR_PICKUP
    private Long orderId;
    private String consumerName;
    private String consumerPhone;
    private String restaurantName;
    private String deliveryAddress;
    private BigDecimal totalAmount;
}
