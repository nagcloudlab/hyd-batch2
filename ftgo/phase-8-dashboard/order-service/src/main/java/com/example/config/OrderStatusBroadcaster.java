package com.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(Long orderId, String status, String source) {
        Map<String, Object> update = Map.of(
                "orderId", orderId,
                "status", status,
                "source", source,
                "time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );
        log.info("[WEBSOCKET] Broadcasting order #{} → {} ({})", orderId, status, source);
        messagingTemplate.convertAndSend("/topic/order-updates", update);
    }
}
