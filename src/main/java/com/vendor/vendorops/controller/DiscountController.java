package com.vendor.vendorops.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vendor.vendorops.entity.Discount;
import com.vendor.vendorops.entity.Vendor;
import com.vendor.vendorops.repository.DiscountRepository;
import com.vendor.vendorops.repository.VendorRepository;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountRepository discountRepository;
    private final VendorRepository vendorRepository;

    @GetMapping("/vendor/{vendorId}")
    public List<Discount> getDiscounts(@PathVariable Long vendorId) {
        return discountRepository.findByVendorId(vendorId);
    }

    @GetMapping("/vendor/{vendorId}/active")
    public List<Discount> getActiveDiscounts(@PathVariable Long vendorId) {
        return discountRepository.findByVendorIdAndActiveTrue(vendorId);
    }

    @PostMapping("/vendor/{vendorId}")
    public ResponseEntity<?> addDiscount(@PathVariable Long vendorId, @RequestBody Discount discount) {
        try {
            Vendor vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));
            discount.setVendor(vendor);
            return ResponseEntity.ok(discountRepository.save(discount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{discountId}")
    public Discount updateDiscount(@PathVariable Long discountId, @RequestBody Discount updated) {
        Discount existing = discountRepository.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        existing.setCode(updated.getCode());
        existing.setDescription(updated.getDescription());
        existing.setType(updated.getType());
        existing.setValue(updated.getValue());
        existing.setMinOrderAmount(updated.getMinOrderAmount());
        existing.setActive(updated.getActive());
        existing.setValidFrom(updated.getValidFrom());
        existing.setValidUntil(updated.getValidUntil());
        return discountRepository.save(existing);
    }

    @DeleteMapping("/{discountId}")
    public ResponseEntity<?> deleteDiscount(@PathVariable Long discountId) {
        discountRepository.deleteById(discountId);
        return ResponseEntity.ok().body("{\"message\":\"Discount deleted\"}");
    }

    @PostMapping("/vendor/{vendorId}/validate")
    public ResponseEntity<?> validateCode(@PathVariable Long vendorId, @RequestBody Map<String, String> body) {
        String code = body.get("code");
        return discountRepository.findByCodeAndVendorId(code, vendorId)
                .filter(Discount::getActive)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired discount code")));
    }
}
