package com.vendor.microbizmax.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vendor.microbizmax.entity.Product;
import com.vendor.microbizmax.service.ProductService;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/vendor/{vendorId}")
    public List<Product> getProducts(@PathVariable Long vendorId) {
        return productService.getProductsByVendor(vendorId);
    }

    @GetMapping("/{productId}")
    public Product getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

    @PostMapping("/vendor/{vendorId}")
    public Product addProduct(@PathVariable Long vendorId, @RequestBody Product product) {
        return productService.addProduct(vendorId, product);
    }

    @PutMapping("/{productId}")
    public Product updateProduct(@PathVariable Long productId, @RequestBody Product product) {
        return productService.updateProduct(productId, product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok().body("{\"message\":\"Product deleted\"}");
    }

    @GetMapping("/vendor/{vendorId}/categories")
    public List<String> getCategories(@PathVariable Long vendorId) {
        return productService.getCategories(vendorId);
    }

    @GetMapping("/vendor/{vendorId}/low-stock")
    public Long getLowStockCount(@PathVariable Long vendorId) {
        return productService.getLowStockCount(vendorId);
    }
}
