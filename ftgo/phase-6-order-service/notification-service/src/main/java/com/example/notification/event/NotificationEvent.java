package com.example.notification.event;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NotificationEvent {

    private String eventType;
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
