package com.example.consumer;

import com.example.entity.FoodOrder;
import com.example.entity.OrderStatus;
import com.example.event.DeliveryEvent;
import com.example.repository.FoodOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventConsumer {

    private final FoodOrderRepository foodOrderRepository;

    @KafkaListener(topics = "ftgo.delivery.events", groupId = "ftgo-monolith")
    @Transactional
    public void handleDeliveryEvent(DeliveryEvent event) {
        log.info("[KAFKA-CONSUMER] Received {} for order #{}", event.getEventType(), event.getOrderId());

        FoodOrder order = foodOrderRepository.findById(event.getOrderId())
                .orElse(null);

        if (order == null) {
            log.warn("Order #{} not found, skipping delivery event", event.getOrderId());
            return;
        }

        // Update denormalized delivery status
        order.setDeliveryStatus(event.getDeliveryStatus());

        // Update order status based on delivery lifecycle
        switch (event.getEventType()) {
            case "DELIVERY_PICKED_UP" -> {
                order.setStatus(OrderStatus.PICKED_UP);
                log.info("Order #{} status updated to PICKED_UP", event.getOrderId());
            }
            case "DELIVERY_COMPLETED" -> {
                order.setStatus(OrderStatus.DELIVERED);
                log.info("Order #{} status updated to DELIVERED", event.getOrderId());
            }
            case "DELIVERY_ASSIGNED" -> {
                log.info("Order #{} delivery assigned to courier", event.getOrderId());
            }
            default -> log.warn("Unknown delivery event type: {}", event.getEventType());
        }

        foodOrderRepository.save(order);
    }
}
