package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "food_order_id")
    private FoodOrder foodOrder;

    // Denormalized — menu item data now lives in restaurant-service
    private Long menuItemId;
    private String menuItemName;

    private int quantity;
    private BigDecimal price;
}
