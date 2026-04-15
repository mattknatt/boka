# boka. 

[![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Fly.io](https://img.shields.io/badge/Fly.io-4433FF?style=for-the-badge&logo=fly.io&logoColor=white)](https://fly.io/)

**boka.** is a high-performance, full-stack gym class booking application. It demonstrates a modern architectural approach to building scalable web applications with a focus on developer experience, modular design, and cloud-native deployment.

---

## 🚀 Technical Highlights

This project showcases a robust engineering foundation:

- **State-of-the-Art Java:** Leverages **Java 25 (LTS)** features for modern, efficient backend logic.
- **Hexagonal Architecture Principles:** Utilizes **Ports and Adapters** to decouple core business logic from infrastructure, ensuring high testability and maintainability.
- **Unified Development Workflow:** A root-level orchestration setup using `concurrently` that manages Spring Boot, Vite, and Dockerized Postgres with a single `npm start` command.
- **Secure Authentication:** Seamless integration of **Google OAuth2** and local **BCrypt-hashed** credentials, handled securely via Spring Security with custom session management.
- **Type-Safe Frontend:** A **React 19** application built with **TypeScript** and **Vite**, featuring a dynamic view-state management system.
- **Optimized SQL Performance:** Custom JPQL queries with grouped aggregations to eliminate N+1 problems during data enrichment.

---

## ✨ Core Features

- **Intuitive Discovery:** Dual entry points for searching specific **Classes** or exploring local **Gyms**.
- **Atomic Booking System:** Robust reservation engine with server-side validation to prevent overbooking and double-bookings.
- **Personal Schedule Management:** A dedicated **"My Bookings"** dashboard for users to track and cancel their upcoming sessions.
- **Account Customization:** Integrated **Settings** portal for managing profile information.
- **Immersive UI/UX:** Modern, responsive design featuring a fixed parallax-style background and instant visual feedback for all user actions.

---

## ☁️ Cloud Architecture & Deployment

**boka.** is built for the modern cloud, utilizing a serverless and containerized ecosystem:

### 🌍 Hosting & Compute
- **Platform:** [Fly.io](https://fly.io/)
- **Strategy:** Multi-stage **Docker** build that bundles optimized React production assets directly into the Spring Boot executable, resulting in a single, high-performance deployment unit.

### 💾 Database
- **Provider:** [Neon](https://neon.tech/)
- **Type:** Serverless Postgres
- **Benefit:** Provides instant scalability and zero-cold-start performance, perfectly suited for modern web workloads.

### 🤖 CI/CD Pipeline
- **Tool:** **GitHub Actions**
- **Integration & Deployment:** Automated build, test, and deploy pipelines (`ci.yml`, `fly-deploy.yml`) ensure every commit is production-ready.

---

## 🚦 Getting Started

### Prerequisites
- JDK 25
- Node.js 20+
- Docker (for local database)

### Local Startup
Clone the repository and run the following in the root directory:

```bash
npm run install:all  # Setup dependencies
npm start            # Launch the full stack
```

**Local Dev Credentials:** 
- Admin: `admin@boka.se` / `password123`
- Member: `karl@example.com` / `password123`

---

## 📄 License
This project is licensed under the MIT License.
