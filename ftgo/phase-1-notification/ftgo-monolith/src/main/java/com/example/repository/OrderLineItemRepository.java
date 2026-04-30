package com.example.repository;

import com.example.entity.OrderLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineItemRepository extends JpaRepository<OrderLineItem, Long> {
}
