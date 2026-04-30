package com.example.consumer;

import com.example.entity.FoodOrder;
import com.example.entity.OrderStatus;
import com.example.event.KitchenEvent;
import com.example.repository.FoodOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @RequiredArgsConstructor @Slf4j
public class KitchenStatusConsumer {

    private final FoodOrderRepository foodOrderRepository;

    @KafkaListener(topics = "ftgo.kitchen.events", groupId = "ftgo-monolith")
    @Transactional
    public void handleKitchenEvent(KitchenEvent event) {
        // Ignore our own ORDER_PLACED events
        if ("ORDER_PLACED".equals(event.getEventType())) {
            return;
        }

        if ("TICKET_STATUS_UPDATED".equals(event.getEventType())) {
            String newStatus = event.getItems(); // status carried in items field
            log.info("[KAFKA-CONSUMER] Kitchen status update for order #{} → {}", event.getOrderId(), newStatus);

            FoodOrder order = foodOrderRepository.findById(event.getOrderId()).orElse(null);
            if (order == null) {
                log.warn("Order #{} not found, skipping kitchen status update", event.getOrderId());
                return;
            }

            try {
                order.setStatus(OrderStatus.valueOf(newStatus));
                foodOrderRepository.save(order);
                log.info("Order #{} status updated to {}", event.getOrderId(), newStatus);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown order status: {}", newStatus);
            }
        }
    }
}
