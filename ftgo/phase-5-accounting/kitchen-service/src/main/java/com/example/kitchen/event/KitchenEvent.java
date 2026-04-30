package com.example.kitchen.event;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class KitchenEvent {
    private String eventType;       // ORDER_PLACED
    private Long orderId;
    private Long restaurantId;
    private String restaurantName;
    private String consumerName;
    private String consumerPhone;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private String items;
}
