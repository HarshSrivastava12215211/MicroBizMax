package com.vendor.vendorops.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.vendor.vendorops.service.AnalyticsService;

import java.time.LocalDate;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/vendor/{vendorId}/dashboard")
    public Map<String, Object> getDashboardStats(@PathVariable Long vendorId) {
        return analyticsService.getDashboardStats(vendorId);
    }

    @GetMapping("/vendor/{vendorId}/monthly")
    public Map<String, Object> getMonthlySales(@PathVariable Long vendorId,
                                                @RequestParam(required = false) Integer year) {
        if (year == null) year = LocalDate.now().getYear();
        return analyticsService.getMonthlySales(vendorId, year);
    }
}
