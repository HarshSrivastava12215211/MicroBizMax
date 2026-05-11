# 🚀 MicroBizMax (formerly VendorOps)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Render](https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white)

**MicroBizMax** is a premium, full-stack vendor management platform designed with a modern **Light Blue & White Glassmorphism** aesthetic. It empowers small to medium-sized business owners (vendors) to digitize their inventory, track sales, manage customers, and apply smart discounts—all through a sleek, professional interface.

![MicroBizMax Preview](https://via.placeholder.com/1200x600/eff6ff/3b82f6?text=MicroBizMax+Smart+Vendor+Platform)

---

## ✨ Key Features

### 🏢 Vendor Ecosystem
- **Smart Onboarding:** Seamless registration flow for vendors with store profile setup.
- **Multilingual Support:** Full localization in **English**, **Hindi (हिन्दी)**, and **Punjabi (ਪੰਜਾਬੀ)**.
- **AI-Powered Chatbot:** Integrated Gemini AI assistant to help users navigate the platform and manage tasks.

### 📊 Powerful Dashboard
- **Live Analytics:** Real-time tracking of Sales, Profits, and Customer growth.
- **Visual Insights:** Interactive charts (Chart.js) showing monthly performance trends.
- **Low Stock Alerts:** Automatic notifications for products running low on inventory.

### 🔐 Administrative Control
- **Dedicated Admin Portal:** Secure gateway for system administrators to manage all vendors, products, and platform-wide metrics.
- **Restricted Access:** Hardened login security for administrative functions.

### 📦 Management Suite
- **Inventory Control:** Add, edit, and categorize products with cost/price tracking.
- **Customer CRM:** Maintain a database of customers and track their loyalty (VIP/Repeat/New status).
- **Order Management:** View detailed transaction histories and payment methods.
- **Smart Discounts:** Create and manage promo codes with minimum order requirements and expiry dates.

---

## 🛠️ Technology Stack

- **Backend (Server-Side):** Java 17, Spring Boot (REST API), Spring Data JPA, Spring Security
- **Database (Persistence):** PostgreSQL (Hosted on NeonDB)
- **Frontend (Client-Side):** HTML5, CSS3 (Custom Glassmorphism Design System), Vanilla JavaScript (ES6+)
- **Visuals & Utils:** Chart.js, Font Awesome 6, Google Fonts (Inter), Gemini AI (Chatbot)
- **Deployment:** Dockerized and hosted on **Render**

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.6+
- PostgreSQL instance

### Local Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/HarshSrivastava12215211/MicroBizMax.git
   cd MicroBizMax
   ```

2. **Configure Database:**
   Update `src/main/resources/application.properties` with your PostgreSQL credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/microbizmax
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Run the Application:**
   ```bash
   mvn spring-boot:run
   ```
   The platform will be available at `http://localhost:9090`.

---

## ☁️ Deployment

This project is live and cloud-ready:
- **Live Demo:** [https://microbizmax.onrender.com](https://microbizmax.onrender.com)
- **Database:** Hosted on [Neon.tech](https://neon.tech/) (Free PostgreSQL).
- **Hosting:** Deployed on [Render.com](https://render.com/).
- **Build:** Render automatically detects the `Dockerfile` and deploys the full-stack Spring Boot application.

---

## 🎨 Design System
The platform utilizes a custom-built design system defined in `app.css`:
- **Primary Palette:** Blue-50 to Blue-800
- **Aesthetic:** Glassmorphism (High-blur backgrounds, soft shadows, and translucent cards)
- **Typography:** Inter (Modern sans-serif)

---

## 📜 License
This project is licensed under the MIT License.

---
*Created with ❤️ for modern business management.*
