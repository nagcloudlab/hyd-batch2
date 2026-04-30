package com.example.consumer;

import com.example.config.OrderStatusBroadcaster;
import com.example.entity.FoodOrder;
import com.example.entity.OrderStatus;
import com.example.event.DeliveryEvent;
import com.example.repository.FoodOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @RequiredArgsConstructor @Slf4j
public class DeliveryEventConsumer {

    private final FoodOrderRepository foodOrderRepository;
    private final OrderStatusBroadcaster broadcaster;

    @KafkaListener(topics = "ftgo.delivery.events", groupId = "ftgo-monolith")
    @Transactional
    public void handleDeliveryEvent(DeliveryEvent event) {
        log.info("[KAFKA-CONSUMER] Received {} for order #{}", event.getEventType(), event.getOrderId());
        FoodOrder order = foodOrderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) return;

        order.setDeliveryStatus(event.getDeliveryStatus());
        switch (event.getEventType()) {
            case "DELIVERY_PICKED_UP" -> order.setStatus(OrderStatus.PICKED_UP);
            case "DELIVERY_COMPLETED" -> order.setStatus(OrderStatus.DELIVERED);
        }
        foodOrderRepository.save(order);
        broadcaster.broadcast(event.getOrderId(), order.getStatus().name(), "delivery");
    }
}
