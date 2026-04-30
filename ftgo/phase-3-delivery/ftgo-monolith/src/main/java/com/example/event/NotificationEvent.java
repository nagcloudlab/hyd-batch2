package com.example.event;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NotificationEvent {

    private String eventType;       // ORDER_CONFIRMED, ORDER_STATUS_UPDATED, DELIVERY_ASSIGNED, DELIVERY_PICKED_UP, DELIVERY_COMPLETED
    private Long orderId;
    private String consumerPhone;
    private String consumerEmail;
    private String restaurantPhone;
    private String restaurantName;
    private String courierName;
    private String orderStatus;
    private BigDecimal totalAmount;
    private String message;
}
