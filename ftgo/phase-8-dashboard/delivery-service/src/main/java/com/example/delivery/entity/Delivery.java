package com.example.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Denormalized from order — delivery-service owns this data
    private Long orderId;
    private String consumerName;
    private String consumerPhone;
    private String restaurantName;
    private String deliveryAddress;
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private LocalDateTime assignedTime;
    private LocalDateTime pickedUpTime;
    private LocalDateTime deliveredTime;
}
