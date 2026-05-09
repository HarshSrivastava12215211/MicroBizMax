package com.vendor.vendorops.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ChatController {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private static final String SYSTEM_PROMPT = """
            You are a friendly and helpful customer support assistant for VendorOps — a vendor management portal for small business owners.
            Your job is to answer user questions about how to use the VendorOps platform. Be concise, clear, and friendly.

            Here is a complete guide to every feature of VendorOps:

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

            Always be helpful and guide users step by step. If asked something outside VendorOps, politely say you can only help with VendorOps features.
            """;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.getOrDefault("message", "").trim();

        if (userMessage.isEmpty()) {
            return ResponseEntity.ok(Map.of("reply", "Please type a message!"));
        }

        // If no API key configured, fall back to keyword replies
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return ResponseEntity.ok(Map.of("reply", getFallbackReply(userMessage)));
        }

        try {
            String reply = callGemini(userMessage);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("reply", getFallbackReply(userMessage)));
        }
    }

    private String callGemini(String userMessage) {
        String url = GEMINI_URL + geminiApiKey;

        // Build request body
        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text", SYSTEM_PROMPT))
        );

        Map<String, Object> userPart = Map.of("text", userMessage);
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
        return "I can help with: login/register, adding customers, products, sales, discounts, and managing your profile. What would you like to know?";
    }
}