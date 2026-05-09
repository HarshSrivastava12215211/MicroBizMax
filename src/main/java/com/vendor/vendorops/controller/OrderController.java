package com.vendor.vendorops.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vendor.vendorops.entity.SalesOrder;
import com.vendor.vendorops.service.OrderService;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/vendor/{vendorId}")
    public List<SalesOrder> getOrders(@PathVariable Long vendorId) {
        return orderService.getOrdersByVendor(vendorId);
    }

    @GetMapping("/{orderId}")
    public SalesOrder getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/customer/{customerId}")
    public List<SalesOrder> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    @PostMapping("/vendor/{vendorId}")
    public ResponseEntity<?> createOrder(@PathVariable Long vendorId, @RequestBody Map<String, Object> payload) {
        try {
            Long customerId = payload.get("customerId") != null ?
                    Long.valueOf(payload.get("customerId").toString()) : null;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

            String paymentMethod = (String) payload.getOrDefault("paymentMethod", "CASH");
            String discountCode = (String) payload.get("discountCode");

            SalesOrder order = orderService.createOrder(vendorId, customerId, items, paymentMethod, discountCode);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
