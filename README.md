# boka.

[![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Fly.io](https://img.shields.io/badge/Fly.io-4433FF?style=for-the-badge&logo=fly.io&logoColor=white)](https://fly.io/)

**boka.** is a high-performance, full-stack gym class booking application. It demonstrates a modern architectural approach to building scalable web applications with a focus on developer experience and cloud-native deployment.

---

## 🚀 Technical Highlights

This project showcases a robust engineering foundation:

- **State-of-the-Art Java:** Leverages **Java 25** features for modern, efficient backend logic.
- **Unified Development Workflow:** A root-level orchestration setup using `concurrently` that manages Spring Boot, Vite, and Dockerized Postgres with a single `npm start` command.
- **Secure Authentication:** Seamless integration of **Google OAuth2** and local **BCrypt-hashed** credentials, handled securely via Spring Security.
- **Type-Safe Frontend:** A **React 18** application built with **TypeScript** and **Vite**, ensuring reliable data structures from the API to the UI.
- **Clean Architecture:** Strict adherence to the Service-Repository pattern, utilizing DTOs and Mappers to maintain a decoupled and testable codebase.

---

## ☁️ Cloud Architecture & Deployment

**boka.** is built for the modern cloud, utilizing a serverless and containerized ecosystem:

### 🌍 Hosting & Compute
- **Platform:** [Fly.io](https://fly.io/)
- **Strategy:** Multi-stage **Docker** build that bundles the optimized React production assets directly into the Spring Boot executable, resulting in a single, high-performance deployment unit.

### 💾 Database
- **Provider:** [Neon](https://neon.tech/)
- **Type:** Serverless Postgres
- **Benefit:** Provides instant scalability and branching capabilities, allowing the application to handle variable loads with zero cold starts.

### 🤖 CI/CD Pipeline
- **Tool:** **GitHub Actions**
- **Continuous Integration (`ci.yml`):** Automated builds and test execution on every pull request to ensure code quality and prevent regressions.
- **Continuous Deployment (`fly-deploy.yml`):** Automated container builds and zero-downtime deployments to Fly.io upon merging to the `main` branch.

---

## ✨ Core Features

- **Dynamic Class Search:** Instantly find upcoming classes with a filtered search engine.
- **Atomic Bookings:** Robust reservation system with server-side validation against overbooking.
- **Mobile-Responsive UI:** Custom CSS-driven design that scales elegantly from desktop to mobile.
- **Live Availability:** Real-time updates of available spots as users book classes.

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

## 🛠 Tech Stack

- **Backend:** Spring Boot 4.0.2, Spring Data JPA, Hibernate, Spring Security.
- **Frontend:** React 18, TypeScript, Vite, Vanilla CSS.
- **Infrastructure:** Docker, Fly.io, Neon Postgres, GitHub Actions.

---

## 📄 License
This project is licensed under the MIT License.
