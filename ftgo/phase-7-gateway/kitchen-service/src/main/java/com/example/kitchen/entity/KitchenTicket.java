package com.example.kitchen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KitchenTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long restaurantId;
    private String restaurantName;
    private String consumerName;
    private String consumerPhone;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private String items;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private LocalDateTime createdAt;
}
