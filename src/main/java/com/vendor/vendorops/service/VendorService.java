package com.vendor.vendorops.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vendor.vendorops.dto.VendorRegistrationRequest;
import com.vendor.vendorops.entity.Vendor;
import com.vendor.vendorops.repository.VendorRepository;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;

    public Vendor registerVendor(VendorRegistrationRequest request) {

        Vendor vendor = Vendor.builder()
                .name(request.getName())
                .shopName(request.getShopName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        return vendorRepository.save(vendor);
    }
}