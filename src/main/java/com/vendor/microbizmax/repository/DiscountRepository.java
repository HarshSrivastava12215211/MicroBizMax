package com.vendor.microbizmax.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vendor.microbizmax.entity.Discount;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    List<Discount> findByVendorId(Long vendorId);
    List<Discount> findByVendorIdAndActiveTrue(Long vendorId);
    Optional<Discount> findByCodeAndVendorId(String code, Long vendorId);
}
