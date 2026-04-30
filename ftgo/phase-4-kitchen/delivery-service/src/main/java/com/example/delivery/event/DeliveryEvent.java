package com.example.delivery.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DeliveryEvent {

    private String eventType;       // DELIVERY_ASSIGNED, DELIVERY_PICKED_UP, DELIVERY_COMPLETED
    private Long orderId;
    private Long deliveryId;
    private String deliveryStatus;
    private Long courierId;
    private String courierName;
}
