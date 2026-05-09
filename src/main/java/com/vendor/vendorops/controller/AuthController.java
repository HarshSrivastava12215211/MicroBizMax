package com.vendor.vendorops.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.vendor.vendorops.dto.LoginRequest;
import com.vendor.vendorops.dto.VendorRegistrationRequest;
import com.vendor.vendorops.entity.Vendor;
import com.vendor.vendorops.repository.VendorRepository;
import com.vendor.vendorops.service.VendorService;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final VendorService vendorService;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Vendor register(@RequestBody VendorRegistrationRequest request) {

        return vendorService.registerVendor(request);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Vendor vendor = vendorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (!passwordEncoder.matches(request.getPassword(), vendor.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        return ResponseEntity.ok(vendor);
    }
}