package com.vendor.microbizmax.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.vendor.microbizmax.service.AnalyticsService;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ChatController {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final AnalyticsService analyticsService;
    private final RestTemplate restTemplate = new RestTemplate();

    public ChatController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    private static final String SYSTEM_PROMPT = """
            You are a friendly and helpful assistant for MicroBizMax — a vendor management portal for small business owners.
            Answer questions about the MicroBizMax platform, small business operations, customers, products, sales, discounts,
            inventory, analytics, and practical store-management decisions. Use the live business context when it is provided.
            Be concise, clear, and friendly.

            Here is a complete guide to every feature of MicroBizMax:

            ## Login & Registration
            - **Login**: Go to the login page at /login.html. Enter your registered email and password. Click Login.
            - **Register**: Click "Register" on the login page or go to /register.html. Fill in your name, shop name, email, password, phone, and address.
            - **Logout**: Click the 🚪 Logout button in the sidebar footer on any dashboard page.

            ## Dashboard
            - The dashboard shows: Total Sales (₹), Total Profit (₹), number of Products, Orders Today, Total Customers, and Repeat Customers.
            - There is also a Monthly Sales & Profit bar chart and a Quick Actions panel.
            - Quick Actions let you: Add Product, New Sale, Add Customer, or Create Discount directly.

            ## Products
            - Go to /products.html from the sidebar.
            - Click "+ Add Product" to open a form. Fill in: Product Name, Category, Price (₹), Cost Price (₹), Stock Quantity, and Unit.
            - Products are listed in a table with Edit (✏️) and Delete (🗑️) buttons.
            - Low stock products are highlighted automatically.

            ## Sales / Orders
            - Go to /sales.html from the sidebar.
            - Click "+ New Sale" to create a sale. Select a customer, add products (choose product + quantity), optionally apply a discount code, and set payment method (Cash/UPI/Card).
            - All sales appear in a table with date, customer, total, payment method, and status.

            ## Customers
            - Go to /customers.html from the sidebar.
            - Click "+ Add Customer" to add a customer. Fill in: Name, Phone, Email (optional), Address (optional).
            - Customers are shown in a table with: name, phone, email, orders count, total spent, and loyalty status.
            - Repeat customers (more than 1 order) are shown in a highlighted section.

            ## Discounts
            - Go to /discounts.html from the sidebar.
            - Click "+ Add Discount" to create a discount code. Fill in: Code, Discount Type (Percentage or Fixed), Value, Min Order Amount, Expiry Date.
            - Discounts can be applied during sale creation.

            ## Profile
            - Go to /profile.html from the sidebar.
            - View and edit your shop name, email, phone number, and address.
            - Click "Edit Profile" to make changes and save.

            ## Admin Portal
            - Admins can log in at /admin-login.html with email: admin@gmail.com and password: admin12345.
            - The admin dashboard shows all vendors, customers, products, orders, and discounts across all accounts.

            Always be helpful and guide users step by step. If a question is unrelated to MicroBizMax, still answer helpfully
            when it is a normal general-knowledge or business question. If the user asks for unsafe, private, illegal, or
            sensitive information, refuse briefly and redirect to a safe alternative.
            """;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> request) {
        String userMessage = String.valueOf(request.getOrDefault("message", "")).trim();
        Long vendorId = parseVendorId(request.get("vendorId"));

        if (userMessage.isEmpty()) {
            return ResponseEntity.ok(Map.of("reply", "Please type a message!"));
        }

        if (vendorId != null) {
            String directAnalyticsReply = getDirectAnalyticsReply(userMessage, vendorId);
            if (directAnalyticsReply != null) {
                return ResponseEntity.ok(Map.of("reply", directAnalyticsReply));
            }
        }

        // If no API key configured, fall back to keyword replies
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return ResponseEntity.ok(Map.of("reply", getFallbackReply(userMessage)));
        }

        try {
            String reply = callGemini(userMessage, vendorId);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("reply", getFallbackReply(userMessage)));
        }
    }

    private String callGemini(String userMessage, Long vendorId) {
        String url = GEMINI_URL.formatted(geminiModel, geminiApiKey);
        String prompt = userMessage;
        if (vendorId != null) {
            prompt = buildBusinessContext(vendorId) + "\n\nUser question: " + userMessage;
        }

        // Build request body
        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text", SYSTEM_PROMPT))
        );

        Map<String, Object> userPart = Map.of("text", prompt);
        Map<String, Object> userContent = Map.of(
                "role", "user",
                "parts", List.of(userPart)
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("system_instruction", systemInstruction);
        body.put("contents", List.of(userContent));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // Parse response: candidates[0].content.parts[0].text
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

    private Long parseVendorId(Object rawVendorId) {
        if (rawVendorId == null) return null;
        try {
            if (rawVendorId instanceof Number number) {
                return number.longValue();
            }
            String value = String.valueOf(rawVendorId).trim();
            return value.isEmpty() ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getDirectAnalyticsReply(String message, Long vendorId) {
        String m = message.toLowerCase();
        boolean asksSales = m.contains("sale") || m.contains("revenue") || m.contains("earning") || m.contains("income");
        boolean asksProfit = m.contains("profit");
        boolean asksOrders = m.contains("order");
        boolean asksCustomers = m.contains("customer");
        boolean asksProducts = m.contains("product") || m.contains("stock") || m.contains("inventory");
        boolean asksThisMonth = m.contains("this month") || m.contains("current month") || m.contains("monthly");
        boolean asksToday = m.contains("today");
        boolean asksTotal = m.contains("total") || m.contains("overall") || m.contains("all time");
        boolean asksForMetric = asksThisMonth || asksToday || asksTotal || m.contains("how many") || m.contains("how much")
                || m.contains("what is") || m.contains("what's") || m.contains("whats") || m.contains("show me")
                || m.contains("tell me") || m.contains("count");

        if (!(asksSales || asksProfit || asksOrders || asksCustomers || asksProducts)) {
            return null;
        }

        if (!asksForMetric) {
            return null;
        }

        Map<String, Object> dashboard = analyticsService.getDashboardStats(vendorId);
        Map<String, Object> monthly = analyticsService.getMonthlySales(vendorId, LocalDate.now().getYear());
        int monthIndex = LocalDate.now().getMonthValue() - 1;

        if (asksThisMonth || (asksSales && !asksTotal && !asksToday)) {
            double monthSales = getMonthlyValue(monthly, "sales", monthIndex);
            double monthProfit = getMonthlyValue(monthly, "profit", monthIndex);
            String monthName = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            if (asksProfit && !asksSales) {
                return "Your profit for " + monthName + " is " + rupees(monthProfit) + ".";
            }
            return "Your sales for " + monthName + " are " + rupees(monthSales) + ", with " + rupees(monthProfit) + " profit.";
        }

        if (asksToday || asksOrders) {
            long ordersToday = asLong(dashboard.get("ordersToday"));
            return "You have " + ordersToday + " order" + (ordersToday == 1 ? "" : "s") + " today.";
        }

        if (asksProfit) {
            return "Your total profit is " + rupees(asDouble(dashboard.get("totalProfit"))) + ".";
        }

        if (asksSales || asksTotal) {
            return "Your total sales are " + rupees(asDouble(dashboard.get("totalSales"))) + ".";
        }

        if (asksCustomers) {
            return "You have " + asLong(dashboard.get("totalCustomers")) + " customers, including "
                    + asLong(dashboard.get("repeatedCustomers")) + " repeat customers.";
        }

        if (asksProducts) {
            return "You have " + asLong(dashboard.get("totalProducts")) + " products, with "
                    + asLong(dashboard.get("lowStockProducts")) + " low-stock items.";
        }

        return null;
    }

    private String buildBusinessContext(Long vendorId) {
        Map<String, Object> dashboard = analyticsService.getDashboardStats(vendorId);
        Map<String, Object> monthly = analyticsService.getMonthlySales(vendorId, LocalDate.now().getYear());
        int monthIndex = LocalDate.now().getMonthValue() - 1;
        String monthName = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        return """
                Live MicroBizMax business context:
                - Vendor ID: %d
                - Total sales: %s
                - Total profit: %s
                - Orders today: %d
                - Total customers: %d
                - Repeat customers: %d
                - Total products: %d
                - Low-stock products: %d
                - %s sales: %s
                - %s profit: %s
                """.formatted(
                vendorId,
                rupees(asDouble(dashboard.get("totalSales"))),
                rupees(asDouble(dashboard.get("totalProfit"))),
                asLong(dashboard.get("ordersToday")),
                asLong(dashboard.get("totalCustomers")),
                asLong(dashboard.get("repeatedCustomers")),
                asLong(dashboard.get("totalProducts")),
                asLong(dashboard.get("lowStockProducts")),
                monthName,
                rupees(getMonthlyValue(monthly, "sales", monthIndex)),
                monthName,
                rupees(getMonthlyValue(monthly, "profit", monthIndex))
        );
    }

    private double getMonthlyValue(Map<String, Object> monthly, String key, int monthIndex) {
        Object values = monthly.get(key);
        if (values instanceof List<?> list && monthIndex >= 0 && monthIndex < list.size()) {
            return asDouble(list.get(monthIndex));
        }
        return 0;
    }

    private double asDouble(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        if (value == null) return 0;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long asLong(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return 0;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String rupees(double value) {
        return "₹" + String.format(Locale.ENGLISH, "%,.2f", value);
    }

    private String getFallbackReply(String message) {
        String m = message.toLowerCase();
        if (m.contains("login") || m.contains("sign in"))
            return "To login, go to /login.html and enter your email and password. Need help registering? Click the Register link on the login page.";
        if (m.contains("register") || m.contains("sign up") || m.contains("account"))
            return "To register, click 'Register' on the login page or visit /register.html. Fill in your name, shop name, email, and password.";
        if (m.contains("customer") && m.contains("add"))
            return "To add a customer, go to the Customers page and click '+ Add Customer'. Fill in the name, phone, and optional email/address.";
        if (m.contains("customer"))
            return "The Customers page shows all your customers with orders, total spent, and loyalty status. You can add, edit, or view repeat customers.";
        if (m.contains("product") && m.contains("add"))
            return "To add a product, go to the Products page and click '+ Add Product'. Enter the name, price, cost price, and stock quantity.";
        if (m.contains("product"))
            return "The Products page lists all your products. You can add, edit, or delete products. Low stock items are highlighted automatically.";
        if (m.contains("sale") || m.contains("order"))
            return "To create a new sale, go to the Sales page and click '+ New Sale'. Select a customer, add products, apply a discount if needed, and choose payment method.";
        if (m.contains("discount"))
            return "To create a discount, go to the Discounts page and click '+ Add Discount'. Set a code, type (Percentage/Fixed), value, and expiry date.";
        if (m.contains("dashboard"))
            return "The Dashboard shows your total sales, profit, products, orders today, and customer count. There's also a monthly sales chart and quick-action buttons.";
        if (m.contains("profile"))
            return "Go to the Profile page to view and edit your shop name, email, phone, and address.";
        if (m.contains("logout") || m.contains("log out"))
            return "Click the 🚪 Logout button at the bottom of the sidebar on any page.";
        if (m.contains("admin"))
            return "Admins can log in at /admin-login.html using email: admin@gmail.com and password: admin12345.";
        if (m.contains("repeat customer") || m.contains("loyal") || m.contains("retention"))
            return "To increase repeat customers, try: 1. Give a small reward after every few purchases. 2. Follow up with customers who bought recently and suggest related products. 3. Create simple offers for frequent buyers, like a weekend discount or bundle deal.";
        if (m.contains("increase") || m.contains("improve") || m.contains("grow") || m.contains("idea") || m.contains("advice"))
            return "A practical way to grow your store is to focus on your fastest-moving products, keep low-stock items available, follow up with recent customers, and create simple offers that encourage repeat purchases.";
        return "I can help with MicroBizMax features, store management, customers, products, sales, discounts, inventory, and general business questions. What would you like to know?";
    }
}
