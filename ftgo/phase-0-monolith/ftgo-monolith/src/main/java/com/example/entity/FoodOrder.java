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

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;
    private LocalDateTime orderTime;
    private String deliveryAddress;

    @OneToMany(mappedBy = "foodOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderLineItem> lineItems;

    @OneToOne(mappedBy = "foodOrder", cascade = CascadeType.ALL)
    private Delivery delivery;

    @OneToOne(mappedBy = "foodOrder", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "foodOrder", cascade = CascadeType.ALL)
    private Bill bill;
}
