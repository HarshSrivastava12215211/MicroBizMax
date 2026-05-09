# 🚀 MicroBizMax (formerly VendorOps)

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

- **Backend:** Java 17, Spring Boot, Spring Data JPA
- **Database:** PostgreSQL
- **Frontend:** Vanilla HTML5, CSS3 (Glassmorphism System), JavaScript (ES6+)
- **Visuals:** Chart.js (Data Visualization), Font Awesome 6 (Icons), Google Fonts (Inter)
- **Deployment:** Dockerized for Render/Koyeb

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.6+
- PostgreSQL instance

### Local Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/HarshSrivastava12215211/advance-java-portfolio.git
   cd advance-java-portfolio
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

This project is cloud-ready. To deploy for free:
1. **Database:** Create a free PostgreSQL instance on [Neon.tech](https://neon.tech/).
2. **Web Service:** Connect this repo to [Render.com](https://render.com/).
3. **Build:** Render will automatically detect the `Dockerfile` and deploy the full-stack application.

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
