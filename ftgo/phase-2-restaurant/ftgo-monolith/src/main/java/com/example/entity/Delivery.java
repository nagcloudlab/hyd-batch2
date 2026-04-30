package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
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

    @OneToOne
    @JoinColumn(name = "food_order_id")
    private FoodOrder foodOrder;

    @ManyToOne
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private LocalDateTime assignedTime;
    private LocalDateTime pickedUpTime;
    private LocalDateTime deliveredTime;
}
