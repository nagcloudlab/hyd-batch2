package com.example.kitchen.service;

import com.example.kitchen.entity.KitchenTicket;
import com.example.kitchen.entity.TicketStatus;
import com.example.kitchen.event.*;
import com.example.kitchen.producer.NotificationEventProducer;
import com.example.kitchen.producer.OrderEventProducer;
import com.example.kitchen.repository.KitchenTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class KitchenTicketService {

    private final KitchenTicketRepository ticketRepository;
    private final OrderEventProducer orderEventProducer;
    private final NotificationEventProducer notificationEventProducer;
    private final com.example.kitchen.producer.KitchenStatusProducer kitchenStatusProducer;

    public KitchenTicket createTicket(KitchenEvent event) {
        // Idempotency
        if (ticketRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Ticket already exists for order #{}, skipping", event.getOrderId());
            return ticketRepository.findByOrderId(event.getOrderId()).get();
        }

        KitchenTicket ticket = KitchenTicket.builder()
                .orderId(event.getOrderId())
                .restaurantId(event.getRestaurantId())
                .restaurantName(event.getRestaurantName())
                .consumerName(event.getConsumerName())
                .consumerPhone(event.getConsumerPhone())
                .deliveryAddress(event.getDeliveryAddress())
                .totalAmount(event.getTotalAmount())
                .items(event.getItems())
                .status(TicketStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        KitchenTicket saved = ticketRepository.save(ticket);
        log.info("Created kitchen ticket #{} for order #{}", saved.getId(), event.getOrderId());
        return saved;
    }

    public void acceptTicket(Long ticketId) {
        KitchenTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        if (ticket.getStatus() != TicketStatus.PENDING)
            throw new RuntimeException("Can only accept a PENDING ticket");

        ticket.setStatus(TicketStatus.ACCEPTED);
        ticketRepository.save(ticket);
        log.info("Ticket #{} → ACCEPTED", ticketId);

        // Update order status in monolith
        kitchenStatusProducer.publishStatusUpdate(ticket.getOrderId(), "APPROVED");

        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("ORDER_STATUS_UPDATED")
                .orderId(ticket.getOrderId())
                .consumerPhone(ticket.getConsumerPhone())
                .orderStatus("APPROVED")
                .build());
    }

    public void startPreparing(Long ticketId) {
        KitchenTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        if (ticket.getStatus() != TicketStatus.ACCEPTED)
            throw new RuntimeException("Can only start preparing an ACCEPTED ticket");

        ticket.setStatus(TicketStatus.PREPARING);
        ticketRepository.save(ticket);
        log.info("Ticket #{} → PREPARING", ticketId);

        kitchenStatusProducer.publishStatusUpdate(ticket.getOrderId(), "PREPARING");

        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("ORDER_STATUS_UPDATED")
                .orderId(ticket.getOrderId())
                .consumerPhone(ticket.getConsumerPhone())
                .orderStatus("PREPARING")
                .build());
    }

    public void markReady(Long ticketId) {
        KitchenTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        if (ticket.getStatus() != TicketStatus.PREPARING)
            throw new RuntimeException("Can only mark ready a PREPARING ticket");

        ticket.setStatus(TicketStatus.READY);
        ticketRepository.save(ticket);
        log.info("Ticket #{} → READY, publishing ORDER_READY_FOR_PICKUP", ticketId);

        kitchenStatusProducer.publishStatusUpdate(ticket.getOrderId(), "READY_FOR_PICKUP");

        // Tell delivery-service: food is ready
        orderEventProducer.publish(OrderEvent.builder()
                .eventType("ORDER_READY_FOR_PICKUP")
                .orderId(ticket.getOrderId())
                .consumerName(ticket.getConsumerName())
                .consumerPhone(ticket.getConsumerPhone())
                .restaurantName(ticket.getRestaurantName())
                .deliveryAddress(ticket.getDeliveryAddress())
                .totalAmount(ticket.getTotalAmount())
                .build());

        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("ORDER_STATUS_UPDATED")
                .orderId(ticket.getOrderId())
                .consumerPhone(ticket.getConsumerPhone())
                .orderStatus("READY_FOR_PICKUP")
                .build());
    }

    public List<KitchenTicket> getTicketsByRestaurant(Long restaurantId) {
        return ticketRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }
}
