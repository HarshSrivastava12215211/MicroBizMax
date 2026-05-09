package com.vendor.vendorops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.vendor.vendorops.entity.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByVendorId(Long vendorId);
    Optional<Customer> findByPhoneAndVendorId(String phone, Long vendorId);

    @Query("SELECT c FROM Customer c WHERE c.vendor.id = :vendorId AND c.totalOrders >= 2 ORDER BY c.totalOrders DESC")
    List<Customer> findRepeatedCustomers(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.vendor.id = :vendorId")
    Long countByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.vendor.id = :vendorId AND c.totalOrders >= 2")
    Long countRepeatedByVendorId(@Param("vendorId") Long vendorId);
}
