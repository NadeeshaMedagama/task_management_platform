# Project Overview

## Introduction

The **Task Management Platform** is a full-stack web application that enables teams and individuals to create, organize, and track tasks through an intuitive interface. It features secure JWT-based authentication, role-based access control, and a complete CI/CD pipeline for automated testing, security scanning, and deployment.

---

## Architecture

The system follows a **three-tier architecture** deployed as Docker containers orchestrated via Docker Compose.

```
┌────────────────────┐     ┌─────────────────────────┐     ┌──────────────────┐
│                    │     │                         │     │                  │
│   Next.js 14       │────▶│   Spring Boot 3.2       │────▶│  PostgreSQL 16   │
│   (Frontend)       │     │   (REST API)            │     │  (Database)      │
│   Port 3000        │◀────│   Port 8080             │◀────│  Port 5432       │
│                    │     │                         │     │                  │
└────────────────────┘     └─────────────────────────┘     └──────────────────┘
     TypeScript                   Java 21 / JWT                 Persistent
     Tailwind CSS                 Spring Security               Volume
     Axios                        Hibernate / JPA
```

### Design Principles

| Principle                  | Implementation                                                      |
|----------------------------|---------------------------------------------------------------------|
| **Layered Architecture**   | Controller → Service → Repository with clear separation of concerns |
| **Stateless Auth**         | JWT bearer tokens — no server-side session storage                  |
| **Role-Based Access**      | `USER` manages own tasks; `ADMIN` manages all tasks and users       |
| **Validation at Boundary** | Jakarta Bean Validation on all DTOs with structured error responses  |
| **Containerized**          | Multi-stage Docker builds for both frontend and backend             |
| **Infrastructure as Code** | Docker Compose for local dev; GitHub Actions for CI/CD              |

---

## Technology Stack

### Backend

| Technology             | Version | Purpose                                      |
|------------------------|---------|----------------------------------------------|
| Java                   | 21      | Language runtime (LTS)                       |
| Spring Boot            | 3.2.3   | Application framework                        |
| Spring Security 6      | —       | Authentication & authorization (JWT + RBAC)  |
| Spring Data JPA        | —       | ORM and repository abstraction               |
| Hibernate              | —       | JPA implementation                           |
| PostgreSQL             | 16      | Relational database                          |
| JJWT                   | 0.11.5  | JWT token generation & validation            |
| SpringDoc OpenAPI      | 2.3     | Swagger UI & API documentation               |
| Jakarta Bean Validation| —       | Input validation with constraint annotations |
| Maven                  | 3.9+    | Build and dependency management              |

### Frontend

| Technology       | Version | Purpose                          |
|------------------|---------|----------------------------------|
| Next.js          | 14.1.3  | React framework (App Router)     |
| React            | 18      | UI library                       |
| TypeScript       | 5       | Type-safe JavaScript             |
| Tailwind CSS     | 3.4     | Utility-first CSS framework      |
| Axios            | 1.6     | HTTP client with interceptors    |
| React Hot Toast  | 2.4     | Toast notification system        |
| Lucide React     | 0.344   | Icon library                     |
| date-fns         | 3.3     | Date formatting utilities        |
| Headless UI      | 1.7     | Accessible UI primitives         |

### DevOps & Infrastructure

| Technology        | Purpose                                          |
|-------------------|--------------------------------------------------|
| Docker            | Container runtime                                |
| Docker Compose    | Multi-service orchestration                      |
| GitHub Actions    | CI/CD pipelines (8 workflows)                    |
| Docker Hub        | Container image registry                         |
| GitHub Packages   | Maven artifact hosting                           |
| CodeQL            | Static Application Security Testing (SAST)       |
| Dependabot        | Automated dependency updates                     |
| GitHub Copilot    | AI-powered code review on pull requests          |

---

## Features

### Authentication & Security
- JWT-based stateless authentication with configurable token expiration
- Password hashing with BCrypt
- Role-Based Access Control (RBAC) — `USER` and `ADMIN` roles
- Global exception handler with consistent JSON error responses
- CORS configuration for frontend-backend communication

### Task Management
- Full CRUD operations (Create, Read, Update, Delete)
- Quick status toggle: `TODO` → `IN_PROGRESS` → `DONE`
- Filter tasks by **status** and **priority**
- Server-side **pagination** with configurable page size
- **Sorting** by created date, due date, or priority (ascending/descending)
- Task ownership enforcement — users can only manage their own tasks
- Admin override — administrators can manage all tasks

### Frontend
- Responsive, modern UI built with Tailwind CSS
- Client-side form validation with real-time error feedback
- Automatic JWT token management via Axios interceptors
- Automatic redirect to login on token expiration (401)
- Toast notifications for success and error states

### Developer Experience
- Interactive Swagger UI at `/api/swagger-ui.html`
- OpenAPI 3.0 specification at `/api/v3/api-docs`
- Auto-generated database schema via Hibernate DDL
- Docker Compose for one-command local development
- Hot-reload for frontend development (`npm run dev`)

---

## Project Structure

```
task_management_platform/
│
├── backend/                                # Spring Boot REST API
│   ├── src/main/java/com/taskmanager/
│   │   ├── TaskManagementApplication.java  # Application entry point
│   │   ├── domain/
│   │   │   ├── model/                      # JPA Entities: User, Task
│   │   │   │                               # Enums: Role, TaskStatus, Priority
│   │   │   └── repository/                 # Spring Data JPA Repositories
│   │   ├── application/
│   │   │   ├── dto/
│   │   │   │   ├── request/                # RegisterRequest, LoginRequest,
│   │   │   │   │                           # TaskRequest, UpdateStatusRequest
│   │   │   │   └── response/               # AuthResponse, TaskResponse,
│   │   │   │                               # UserResponse, ApiError
│   │   │   ├── service/                    # AuthService, TaskService, UserService
│   │   │   └── mapper/                     # Entity ↔ DTO mappers
│   │   ├── infrastructure/security/        # JWT filter, SecurityConfig, OpenAPI
│   │   └── web/
│   │       ├── controller/                 # AuthController, TaskController,
│   │       │                               # UserController
│   │       └── exception/                  # GlobalExceptionHandler,
│   │                                       # ResourceNotFoundException,
│   │                                       # AccessDeniedException,
│   │                                       # DuplicateResourceException
│   ├── src/main/resources/
│   │   └── application.yml                 # App configuration
│   ├── Dockerfile                          # Multi-stage build
│   └── pom.xml                             # Maven configuration
│
├── frontend/                               # Next.js 14 application
│   ├── src/
│   │   ├── app/                            # App Router pages
│   │   │   ├── layout.tsx                  # Root layout
│   │   │   ├── page.tsx                    # Root redirect → /tasks
│   │   │   ├── login/page.tsx              # Login page
│   │   │   ├── register/page.tsx           # Registration page
│   │   │   └── tasks/
│   │   │       ├── page.tsx                # Task list with CRUD
│   │   │       └── [id]/                   # Task detail route
│   │   ├── components/                     # TaskCard, TaskForm, FilterBar,
│   │   │                                   # Navbar, Pagination, LoadingSpinner
│   │   ├── context/AuthContext.tsx          # Global auth state (React Context)
│   │   ├── hooks/useTasks.ts               # Data-fetching hook
│   │   ├── lib/
│   │   │   ├── axios.ts                    # Axios instance + interceptors
│   │   │   └── auth.ts                     # Token storage helpers
│   │   └── types/index.ts                  # TypeScript type definitions
│   ├── Dockerfile                          # Multi-stage build
│   └── package.json                        # npm configuration
│
├── .github/
│   ├── dependabot.yml                      # Dependabot configuration
│   └── workflows/                          # 8 GitHub Actions workflows
│
├── docker-compose.yml                      # Full-stack orchestration
├── action.yml                              # GitHub Marketplace action
├── API_DOCUMENTATION.md                    # Detailed API reference
└── README.md                               # Project README
```

---

## Database Schema

### Entity Relationship

```
┌──────────────┐         ┌──────────────────┐
│    users     │         │      tasks       │
├──────────────┤         ├──────────────────┤
│ id (PK)      │◀───┐    │ id (PK)          │
│ username     │    └────│ owner_id (FK)    │
│ email        │         │ title            │
│ password     │         │ description      │
│ role         │         │ status           │
│ created_at   │         │ priority         │
└──────────────┘         │ due_date         │
                         │ created_at       │
                         │ updated_at       │
                         └──────────────────┘
```

### `users` Table

| Column       | Type          | Constraints                      |
|--------------|---------------|----------------------------------|
| `id`         | BIGSERIAL     | PRIMARY KEY, auto-generated      |
| `username`   | VARCHAR(50)   | UNIQUE, NOT NULL                 |
| `email`      | VARCHAR(100)  | UNIQUE, NOT NULL                 |
| `password`   | VARCHAR(255)  | NOT NULL (BCrypt hash)           |
| `role`       | VARCHAR(20)   | NOT NULL — `USER` or `ADMIN`     |
| `created_at` | TIMESTAMP     | DEFAULT `NOW()`                  |

### `tasks` Table

| Column        | Type         | Constraints                                  |
|---------------|--------------|----------------------------------------------|
| `id`          | BIGSERIAL    | PRIMARY KEY, auto-generated                  |
| `title`       | VARCHAR(200) | NOT NULL                                     |
| `description` | TEXT         | NULLABLE                                     |
| `status`      | VARCHAR(20)  | NOT NULL — `TODO`, `IN_PROGRESS`, or `DONE`  |
| `priority`    | VARCHAR(10)  | NOT NULL — `LOW`, `MEDIUM`, or `HIGH`        |
| `due_date`    | DATE         | NULLABLE                                     |
| `created_at`  | TIMESTAMP    | DEFAULT `NOW()`                              |
| `updated_at`  | TIMESTAMP    | Auto-updated on modification                 |
| `owner_id`    | BIGINT       | FOREIGN KEY → `users(id)`, NOT NULL          |

> **Note:** Tables are auto-created by Hibernate on application startup (`ddl-auto: update`). No manual SQL migration scripts are required.

---

## Authorization Model

| Role    | Task Operations                        | User Operations        |
|---------|----------------------------------------|------------------------|
| `USER`  | CRUD on **own tasks** only             | ❌ No access           |
| `ADMIN` | CRUD on **all tasks** in the system    | ✅ List all users      |

New user registrations are assigned the `USER` role by default. Admin promotion requires direct database access:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
```

---

## Related Documentation

| Document                                          | Description                                      |
|---------------------------------------------------|--------------------------------------------------|
| [Setup Guide](./SETUP_GUIDE.md)                   | Installation and local development instructions  |
| [API Reference](./API_REFERENCE.md)               | Complete REST API endpoint documentation         |
| [Deployment & CI/CD](./DEPLOYMENT.md)             | Pipelines, Docker, and production deployment     |
| [Detailed API Docs](../API_DOCUMENTATION.md)      | Extended API documentation with full examples    |

