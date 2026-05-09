package com.vendor.microbizmax.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.vendor.microbizmax.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public Map<String, Object> getDashboardStats(Long vendorId) {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        stats.put("totalSales", orderRepository.getTotalSalesByVendorId(vendorId));
        stats.put("totalProfit", orderRepository.getTotalProfitByVendorId(vendorId));
        stats.put("totalProducts", productRepository.findByVendorId(vendorId).size());
        stats.put("ordersToday", orderRepository.countOrdersTodayByVendorId(vendorId, todayStart));
        stats.put("totalCustomers", customerRepository.countByVendorId(vendorId));
        stats.put("repeatedCustomers", customerRepository.countRepeatedByVendorId(vendorId));
        stats.put("lowStockProducts", productRepository.countLowStockByVendorId(vendorId));

        return stats;
    }

    public Map<String, Object> getMonthlySales(Long vendorId, int year) {
        Map<String, Object> monthly = new LinkedHashMap<>();
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        List<Double> salesData = new ArrayList<>();
        List<Double> profitData = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            LocalDateTime start = LocalDateTime.of(year, m, 1, 0, 0);
            LocalDateTime end = start.withDayOfMonth(start.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);

            Double sales = orderRepository.getSalesByVendorIdAndDateRange(vendorId, start, end);
            Double profit = orderRepository.getProfitByVendorIdAndDateRange(vendorId, start, end);
            salesData.add(sales != null ? sales : 0);
            profitData.add(profit != null ? profit : 0);
        }

        monthly.put("months", months);
        monthly.put("sales", salesData);
        monthly.put("profit", profitData);
        return monthly;
    }
}
