package com.example.delivery.service;

import com.example.delivery.entity.*;
import com.example.delivery.event.*;
import com.example.delivery.producer.DeliveryEventProducer;
import com.example.delivery.producer.NotificationEventProducer;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;
    private final DeliveryEventProducer deliveryEventProducer;
    private final NotificationEventProducer notificationEventProducer;

    public Delivery createDelivery(OrderEvent event) {
        // Idempotency: check if delivery already exists for this order
        if (deliveryRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Delivery already exists for order #{}, skipping", event.getOrderId());
            return deliveryRepository.findByOrderId(event.getOrderId()).get();
        }

        Delivery delivery = Delivery.builder()
                .orderId(event.getOrderId())
                .consumerName(event.getConsumerName())
                .consumerPhone(event.getConsumerPhone())
                .restaurantName(event.getRestaurantName())
                .deliveryAddress(event.getDeliveryAddress())
                .totalAmount(event.getTotalAmount())
                .status(DeliveryStatus.PENDING)
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Created delivery #{} for order #{}", saved.getId(), event.getOrderId());
        return saved;
    }

    public List<Delivery> getPendingDeliveries() {
        return deliveryRepository.findByStatus(DeliveryStatus.PENDING);
    }

    public List<Delivery> getDeliveriesByCourier(Long courierId) {
        return deliveryRepository.findByCourierId(courierId);
    }

    public Delivery acceptDelivery(Long deliveryId, Long courierId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        if (!courier.isAvailable()) {
            throw new RuntimeException("Courier is not available");
        }

        delivery.setCourier(courier);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedTime(LocalDateTime.now());

        courier.setAvailable(false);
        courierRepository.save(courier);

        Delivery saved = deliveryRepository.save(delivery);

        // Publish to ftgo.delivery.events (monolith consumes to update order status)
        deliveryEventProducer.publish(DeliveryEvent.builder()
                .eventType("DELIVERY_ASSIGNED")
                .orderId(delivery.getOrderId())
                .deliveryId(saved.getId())
                .deliveryStatus("ASSIGNED")
                .courierId(courier.getId())
                .courierName(courier.getName())
                .build());

        // Publish to ftgo.notification.events (notification-service consumes)
        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("DELIVERY_ASSIGNED")
                .orderId(delivery.getOrderId())
                .consumerPhone(delivery.getConsumerPhone())
                .courierName(courier.getName())
                .build());

        return saved;
    }

    public Delivery pickUpDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new RuntimeException("Cannot pick up — delivery is not in ASSIGNED status");
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickedUpTime(LocalDateTime.now());
        Delivery saved = deliveryRepository.save(delivery);

        deliveryEventProducer.publish(DeliveryEvent.builder()
                .eventType("DELIVERY_PICKED_UP")
                .orderId(delivery.getOrderId())
                .deliveryId(saved.getId())
                .deliveryStatus("PICKED_UP")
                .build());

        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("DELIVERY_PICKED_UP")
                .orderId(delivery.getOrderId())
                .consumerPhone(delivery.getConsumerPhone())
                .build());

        return saved;
    }

    public Delivery completeDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new RuntimeException("Cannot deliver — order has not been picked up yet");
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredTime(LocalDateTime.now());

        Courier courier = delivery.getCourier();
        courier.setAvailable(true);
        courierRepository.save(courier);

        Delivery saved = deliveryRepository.save(delivery);

        deliveryEventProducer.publish(DeliveryEvent.builder()
                .eventType("DELIVERY_COMPLETED")
                .orderId(delivery.getOrderId())
                .deliveryId(saved.getId())
                .deliveryStatus("DELIVERED")
                .build());

        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("DELIVERY_COMPLETED")
                .orderId(delivery.getOrderId())
                .consumerPhone(delivery.getConsumerPhone())
                .build());

        return saved;
    }
}
