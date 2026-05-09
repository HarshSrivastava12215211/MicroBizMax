package com.vendor.microbizmax.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vendor.microbizmax.entity.*;
import com.vendor.microbizmax.repository.*;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final DiscountRepository discountRepository;
    private final VendorRepository vendorRepository;

    public List<SalesOrder> getOrdersByVendor(Long vendorId) {
        return orderRepository.findByVendorIdOrderByOrderDateDesc(vendorId);
    }

    public List<SalesOrder> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    @Transactional
    public SalesOrder createOrder(Long vendorId, Long customerId, List<Map<String, Object>> items,
                                   String paymentMethod, String discountCode) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
        }

        SalesOrder order = SalesOrder.builder()
                .vendor(vendor)
                .customer(customer)
                .paymentMethod(paymentMethod != null ? paymentMethod : "CASH")
                .status("COMPLETED")
                .build();

        double totalAmount = 0;
        double totalCost = 0;

        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            int qty = Integer.parseInt(item.get("quantity").toString());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            // Reduce stock
            if (product.getStockQuantity() < qty) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepository.save(product);

            double subtotal = product.getPrice() * qty;
            double costSubtotal = (product.getCostPrice() != null ? product.getCostPrice() : 0) * qty;

            OrderItem orderItem = OrderItem.builder()
                    .salesOrder(order)
                    .product(product)
                    .productName(product.getName())
                    .quantity(qty)
                    .unitPrice(product.getPrice())
                    .unitCost(product.getCostPrice())
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);
            totalAmount += subtotal;
            totalCost += costSubtotal;
        }

        // Apply discount
        double discountApplied = 0;
        if (discountCode != null && !discountCode.isEmpty()) {
            Discount discount = discountRepository.findByCodeAndVendorId(discountCode, vendorId).orElse(null);
            if (discount != null && discount.getActive()) {
                if ("PERCENTAGE".equals(discount.getType())) {
                    discountApplied = totalAmount * (discount.getValue() / 100.0);
                } else {
                    discountApplied = discount.getValue();
                }
                if (discount.getMinOrderAmount() != null && totalAmount < discount.getMinOrderAmount()) {
                    discountApplied = 0; // Doesn't meet minimum
                }
            }
            order.setDiscountCode(discountCode);
        }

        order.setTotalAmount(totalAmount - discountApplied);
        order.setTotalCost(totalCost);
        order.setProfit((totalAmount - discountApplied) - totalCost);
        order.setDiscountApplied(discountApplied);

        SalesOrder saved = orderRepository.save(order);

        // Update customer stats
        if (customer != null) {
            customer.setTotalOrders(customer.getTotalOrders() + 1);
            customer.setTotalSpent(customer.getTotalSpent() + saved.getTotalAmount());
            customerRepository.save(customer);
        }

        return saved;
    }

    public SalesOrder getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
