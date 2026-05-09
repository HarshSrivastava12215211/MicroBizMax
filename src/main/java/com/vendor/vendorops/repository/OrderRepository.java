package com.vendor.vendorops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.vendor.vendorops.entity.SalesOrder;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findByVendorIdOrderByOrderDateDesc(Long vendorId);

    @Query("SELECT o FROM SalesOrder o WHERE o.vendor.id = :vendorId AND o.orderDate >= :startDate AND o.orderDate <= :endDate ORDER BY o.orderDate DESC")
    List<SalesOrder> findByVendorIdAndDateRange(@Param("vendorId") Long vendorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM SalesOrder o WHERE o.vendor.id = :vendorId AND o.status = 'COMPLETED'")
    Double getTotalSalesByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT COALESCE(SUM(o.profit), 0) FROM SalesOrder o WHERE o.vendor.id = :vendorId AND o.status = 'COMPLETED'")
    Double getTotalProfitByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM SalesOrder o WHERE o.vendor.id = :vendorId AND o.status = 'COMPLETED' AND o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Double getSalesByVendorIdAndDateRange(@Param("vendorId") Long vendorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.profit), 0) FROM SalesOrder o WHERE o.vendor.id = :vendorId AND o.status = 'COMPLETED' AND o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Double getProfitByVendorIdAndDateRange(@Param("vendorId") Long vendorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM SalesOrder o WHERE o.vendor.id = :vendorId AND o.orderDate >= :startDate")
    Long countOrdersTodayByVendorId(@Param("vendorId") Long vendorId, @Param("startDate") LocalDateTime startDate);

    List<SalesOrder> findByCustomerIdOrderByOrderDateDesc(Long customerId);
}
