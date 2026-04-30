package com.example.delivery.consumer;

import com.example.delivery.event.OrderEvent;
import com.example.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(topics = "ftgo.order.events", groupId = "delivery-service")
    public void handleOrderEvent(OrderEvent event) {
        log.info("[KAFKA-CONSUMER] Received {} for order #{}", event.getEventType(), event.getOrderId());

        if ("ORDER_READY_FOR_PICKUP".equals(event.getEventType())) {
            deliveryService.createDelivery(event);
        } else {
            log.warn("Unknown order event type: {}", event.getEventType());
        }
    }
}
