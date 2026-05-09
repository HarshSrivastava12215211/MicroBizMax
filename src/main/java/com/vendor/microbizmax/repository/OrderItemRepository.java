package com.vendor.microbizmax.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vendor.microbizmax.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
