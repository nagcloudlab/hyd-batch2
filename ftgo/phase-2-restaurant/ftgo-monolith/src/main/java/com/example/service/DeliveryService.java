package com.example.service;

import com.example.entity.*;
import com.example.event.NotificationEvent;
import com.example.event.NotificationEventProducer;
import com.example.repository.CourierRepository;
import com.example.repository.DeliveryRepository;
import com.example.repository.FoodOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final NotificationEventProducer notificationEventProducer;

    public Delivery createDelivery(FoodOrder order) {
        Delivery delivery = Delivery.builder()
                .foodOrder(order)
                .status(DeliveryStatus.PENDING)
                .build();
        return deliveryRepository.save(delivery);
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

        FoodOrder order = delivery.getFoodOrder();
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new RuntimeException("Cannot accept delivery — order is not ready for pickup yet");
        }

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

        // Publish event to Kafka instead of calling NotificationService
        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("DELIVERY_ASSIGNED")
                .orderId(order.getId())
                .consumerPhone(order.getConsumer().getPhone())
                .restaurantPhone(null) // restaurant phone not available locally after extraction
                .courierName(courier.getName())
                .build());

        return deliveryRepository.save(delivery);
    }

    public Delivery pickUpDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new RuntimeException("Cannot pick up — delivery is not in ASSIGNED status");
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickedUpTime(LocalDateTime.now());

        FoodOrder order = delivery.getFoodOrder();
        order.setStatus(OrderStatus.PICKED_UP);
        foodOrderRepository.save(order);

        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("DELIVERY_PICKED_UP")
                .orderId(order.getId())
                .consumerPhone(order.getConsumer().getPhone())
                .build());

        return deliveryRepository.save(delivery);
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

        FoodOrder order = delivery.getFoodOrder();
        order.setStatus(OrderStatus.DELIVERED);
        foodOrderRepository.save(order);

        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("DELIVERY_COMPLETED")
                .orderId(order.getId())
                .consumerPhone(order.getConsumer().getPhone())
                .build());

        return deliveryRepository.save(delivery);
    }
}
