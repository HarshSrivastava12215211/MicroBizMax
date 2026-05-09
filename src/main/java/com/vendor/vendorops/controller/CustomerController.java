package com.vendor.vendorops.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vendor.vendorops.entity.Customer;
import com.vendor.vendorops.entity.Vendor;
import com.vendor.vendorops.repository.CustomerRepository;
import com.vendor.vendorops.repository.VendorRepository;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;

    @GetMapping("/vendor/{vendorId}")
    public List<Customer> getCustomers(@PathVariable Long vendorId) {
        return customerRepository.findByVendorId(vendorId);
    }

    @GetMapping("/vendor/{vendorId}/repeated")
    public List<Customer> getRepeatedCustomers(@PathVariable Long vendorId) {
        return customerRepository.findRepeatedCustomers(vendorId);
    }

    @GetMapping("/{customerId}")
    public Customer getCustomer(@PathVariable Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @PostMapping("/vendor/{vendorId}")
    public ResponseEntity<?> addCustomer(@PathVariable Long vendorId, @RequestBody Customer customer) {
        try {
            Vendor vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));
            customer.setVendor(vendor);
            return ResponseEntity.ok(customerRepository.save(customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{customerId}")
    public Customer updateCustomer(@PathVariable Long customerId, @RequestBody Customer updated) {
        Customer existing = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        return customerRepository.save(existing);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long customerId) {
        customerRepository.deleteById(customerId);
        return ResponseEntity.ok().body("{\"message\":\"Customer deleted\"}");
    }
}
