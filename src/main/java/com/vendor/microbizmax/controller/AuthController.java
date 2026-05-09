package com.vendor.microbizmax.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.vendor.microbizmax.dto.LoginRequest;
import com.vendor.microbizmax.dto.VendorRegistrationRequest;
import com.vendor.microbizmax.entity.Vendor;
import com.vendor.microbizmax.repository.VendorRepository;
import com.vendor.microbizmax.service.VendorService;
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