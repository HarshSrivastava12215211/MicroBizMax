package com.vendor.microbizmax.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.vendor.microbizmax.entity.Vendor;
import com.vendor.microbizmax.repository.VendorRepository;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/{vendorId}")
    public ResponseEntity<?> getProfile(@PathVariable Long vendorId) {
        return vendorRepository.findById(vendorId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{vendorId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long vendorId, @RequestBody Map<String, String> body) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (body.containsKey("name")) vendor.setName(body.get("name"));
        if (body.containsKey("shopName")) vendor.setShopName(body.get("shopName"));
        if (body.containsKey("phone")) vendor.setPhone(body.get("phone"));
        if (body.containsKey("address")) vendor.setAddress(body.get("address"));

        return ResponseEntity.ok(vendorRepository.save(vendor));
    }

    @PutMapping("/{vendorId}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long vendorId, @RequestBody Map<String, String> body) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        String oldPass = body.get("oldPassword");
        String newPass = body.get("newPassword");

        if (!passwordEncoder.matches(oldPass, vendor.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        }

        vendor.setPassword(passwordEncoder.encode(newPass));
        vendorRepository.save(vendor);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}
