package com.vendor.microbizmax.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.vendor.microbizmax.entity.Product;
import com.vendor.microbizmax.entity.Vendor;
import com.vendor.microbizmax.repository.ProductRepository;
import com.vendor.microbizmax.repository.VendorRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;

    public List<Product> getProductsByVendor(Long vendorId) {
        return productRepository.findByVendorId(vendorId);
    }

    public Product addProduct(Long vendorId, Product product) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        product.setVendor(vendor);
        if (product.getActive() == null) product.setActive(true);
        return productRepository.save(product);
    }

    public Product updateProduct(Long productId, Product updated) {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setPrice(updated.getPrice());
        existing.setCostPrice(updated.getCostPrice());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setUnit(updated.getUnit());
        existing.setImageUrl(updated.getImageUrl());
        existing.setActive(updated.getActive());
        return productRepository.save(existing);
    }

    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<String> getCategories(Long vendorId) {
        return productRepository.findDistinctCategoriesByVendorId(vendorId);
    }

    public Long getLowStockCount(Long vendorId) {
        return productRepository.countLowStockByVendorId(vendorId);
    }
}
