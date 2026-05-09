package com.vendor.microbizmax.controller;

import com.vendor.microbizmax.entity.Customer;
import com.vendor.microbizmax.entity.Discount;
import com.vendor.microbizmax.entity.Product;
import com.vendor.microbizmax.entity.SalesOrder;
import com.vendor.microbizmax.entity.Vendor;
import com.vendor.microbizmax.repository.CustomerRepository;
import com.vendor.microbizmax.repository.DiscountRepository;
import com.vendor.microbizmax.repository.OrderRepository;
import com.vendor.microbizmax.repository.ProductRepository;
import com.vendor.microbizmax.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String ADMIN_EMAIL    = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin12345";

    private final VendorRepository    vendorRepository;
    private final CustomerRepository  customerRepository;
    private final ProductRepository   productRepository;
    private final OrderRepository     orderRepository;
    private final DiscountRepository  discountRepository;

    // ── Admin Login ─────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> creds) {
        String email    = creds.getOrDefault("email", "");
        String password = creds.getOrDefault("password", "");

        if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
            return ResponseEntity.ok(Map.of(
                    "role",  "admin",
                    "email", ADMIN_EMAIL,
                    "name",  "Admin"
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid admin credentials"));
    }

    // ── All Vendors ──────────────────────────────────────────────
    @GetMapping("/vendors")
    public ResponseEntity<List<Vendor>> getAllVendors() {
        return ResponseEntity.ok(vendorRepository.findAll());
    }

    // ── All Customers ────────────────────────────────────────────
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    // ── All Products ─────────────────────────────────────────────
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    // ── All Orders ───────────────────────────────────────────────
    @GetMapping("/orders")
    public ResponseEntity<List<SalesOrder>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // ── All Discounts ────────────────────────────────────────────
    @GetMapping("/discounts")
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        return ResponseEntity.ok(discountRepository.findAll());
    }

    // ── Aggregate Stats ──────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalVendors    = vendorRepository.count();
        long totalCustomers  = customerRepository.count();
        long totalProducts   = productRepository.count();
        long totalOrders     = orderRepository.count();
        List<SalesOrder> allOrders = orderRepository.findAll();
        double totalRevenue = allOrders.stream()
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0)
                .sum();

        return ResponseEntity.ok(Map.of(
                "totalVendors",   totalVendors,
                "totalCustomers", totalCustomers,
                "totalProducts",  totalProducts,
                "totalOrders",    totalOrders,
                "totalRevenue",   totalRevenue
        ));
    }
}
