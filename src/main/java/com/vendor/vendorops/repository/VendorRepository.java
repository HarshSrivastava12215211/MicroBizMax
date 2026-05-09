package com.vendor.vendorops.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vendor.vendorops.entity.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByEmail(String email);
}
