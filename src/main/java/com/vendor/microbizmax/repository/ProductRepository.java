package com.vendor.microbizmax.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.vendor.microbizmax.entity.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByVendorId(Long vendorId);
    List<Product> findByVendorIdAndActiveTrue(Long vendorId);
    List<Product> findByVendorIdAndCategory(Long vendorId, String category);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.vendor.id = :vendorId")
    List<String> findDistinctCategoriesByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.vendor.id = :vendorId AND p.stockQuantity <= 5")
    Long countLowStockByVendorId(@Param("vendorId") Long vendorId);
}
