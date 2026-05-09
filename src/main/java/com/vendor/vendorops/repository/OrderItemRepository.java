package com.vendor.vendorops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vendor.vendorops.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
