package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;

    // Denormalized — restaurant data lives in restaurant-service
    private Long restaurantId;
    private String restaurantName;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;
    private LocalDateTime orderTime;
    private String deliveryAddress;

    // Denormalized — delivery data lives in delivery-service
    private String deliveryStatus;

    @OneToMany(mappedBy = "foodOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderLineItem> lineItems;

    @OneToOne(mappedBy = "foodOrder", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "foodOrder", cascade = CascadeType.ALL)
    private Bill bill;
}
