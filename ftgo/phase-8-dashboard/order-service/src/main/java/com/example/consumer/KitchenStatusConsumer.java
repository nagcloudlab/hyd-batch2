package com.example.consumer;

import com.example.config.OrderStatusBroadcaster;
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
    private final OrderStatusBroadcaster broadcaster;

    @KafkaListener(topics = "ftgo.kitchen.events", groupId = "ftgo-monolith")
    @Transactional
    public void handleKitchenEvent(KitchenEvent event) {
        if ("ORDER_PLACED".equals(event.getEventType())) return;
        if (!"TICKET_STATUS_UPDATED".equals(event.getEventType())) return;

        String newStatus = event.getItems();
        log.info("[KAFKA-CONSUMER] Kitchen status update for order #{} -> {}", event.getOrderId(), newStatus);

        FoodOrder order = foodOrderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) return;

        try {
            order.setStatus(OrderStatus.valueOf(newStatus));
            foodOrderRepository.save(order);
            broadcaster.broadcast(event.getOrderId(), newStatus, "kitchen");
        } catch (IllegalArgumentException e) {
            log.warn("Unknown order status: {}", newStatus);
        }
    }
}
