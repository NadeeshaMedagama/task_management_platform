# Task Management Platform

## Overview
A full-stack **Mini Task Management System** built with:
- **Backend**: Spring Boot 3.2, Java 21, PostgreSQL, JWT Security
- **Frontend**: Next.js 14 (App Router), TypeScript, Tailwind CSS, Axios
- **Containerization**: Docker & Docker Compose
- **CI/CD**: GitHub Actions — automated build, test, security scan, release & publish

---

## Project Structure

```
task_management_platform/
├── .github/
│   ├── dependabot.yml                # Dependabot dependency update config
│   └── workflows/
│       ├── ci-cd.yml                 # CI/CD Pipeline (build, test, docker, deploy)
│       ├── codeql.yml                # CodeQL security analysis
│       ├── copilot-review.yml        # GitHub Copilot code review
│       ├── dependabot-auto-merge.yml # Auto-merge Dependabot PRs
│       ├── dependency-updates.yml    # Scheduled dependency audits
│       ├── publish-marketplace.yml   # Publish action to GitHub Marketplace
│       ├── publish-packages.yml      # Publish to GitHub Packages & Docker Hub
│       └── release.yml               # Create GitHub Releases
│
├── action.yml                        # GitHub Marketplace composite action
│
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/taskmanager/
│   │   ├── TaskManagementApplication.java
│   │   ├── domain/
│   │   │   ├── model/               # Entities & Enums (User, Task, Role, TaskStatus, Priority)
│   │   │   └── repository/          # JPA Repositories
│   │   ├── application/
│   │   │   ├── dto/                 # Request & Response DTOs
│   │   │   ├── service/             # Business Logic (AuthService, TaskService, UserService)
│   │   │   └── mapper/              # Entity ↔ DTO Mappers
│   │   ├── infrastructure/
│   │   │   └── security/            # JWT, SecurityConfig, OpenAPI config
│   │   └── web/
│   │       ├── controller/          # REST Controllers
│   │       └── exception/           # GlobalExceptionHandler & Custom Exceptions
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                         # Next.js application
│   ├── src/
│   │   ├── app/                     # Next.js App Router pages
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx             # Root redirect
│   │   │   ├── login/page.tsx
│   │   │   ├── register/page.tsx
│   │   │   └── tasks/page.tsx       # Task list + CRUD
│   │   ├── components/              # Reusable UI components
│   │   ├── context/                 # AuthContext (global auth state)
│   │   ├── hooks/                   # useTasks data-fetching hook
│   │   ├── lib/                     # Axios instance + auth helpers
│   │   └── types/                   # TypeScript type definitions
│   ├── Dockerfile
│   └── package.json
│
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Features

- ✅ **JWT Authentication** — Register & login with BCrypt-hashed passwords
- ✅ **Role-Based Access Control** — `ADMIN` sees all tasks; `USER` manages own tasks only
- ✅ **Full Task CRUD** — Create, read, update, delete tasks
- ✅ **Quick Status Toggle** — Move tasks through TODO → IN_PROGRESS → DONE in one click
- ✅ **Filtering** — Filter by status and priority
- ✅ **Pagination** — Server-side pagination with configurable page size
- ✅ **Sorting** — Sort by created date, due date, or priority (asc/desc)
- ✅ **Input Validation** — Frontend + backend validation with detailed error messages
- ✅ **Global Exception Handler** — Consistent JSON error responses with correct HTTP codes
- ✅ **Swagger UI** — Interactive API documentation at `/api/swagger-ui.html`
- ✅ **Docker Compose** — One-command deployment of all services
- ✅ **CI/CD Pipeline** — Automated build, test, Docker push, and deploy via GitHub Actions
- ✅ **Security Scanning** — CodeQL analysis for Java & JavaScript/TypeScript
- ✅ **Automated Dependency Updates** — Dependabot with auto-merge for minor/patch

---

## CI/CD Pipeline & GitHub Actions

This project includes **9 GitHub Actions workflows** organized across 5 functional areas. Every workflow supports **manual trigger** (`workflow_dispatch`) and scheduled/automatic execution.

### 1️⃣ Build, Test & Deploy (`ci-cd.yml`)

The core pipeline that runs on every push, pull request, weekly schedule, and manual dispatch.

| Stage | What it does |
|-------|-------------|
| **Backend Build & Test** | Compiles with Maven, runs unit tests against a PostgreSQL 16 service container |
| **Frontend Build & Lint** | Installs npm dependencies, lints with ESLint, builds Next.js production bundle |
| **Docker Push — Backend** | Builds backend image and pushes to **Docker Hub** (`<username>/task-management-backend`) |
| **Docker Push — Frontend** | Builds frontend image and pushes to **Docker Hub** (`<username>/task-management-frontend`) |
| **Deploy** | Runs after both images are pushed (add your own kubectl / SSH / compose steps) |

> Backend and frontend Docker images are built & pushed **in parallel** as separate jobs.

```
Push / PR ──▶ Backend Build & Test ──▶ Docker Push Backend ──┐
     │                                                        ├──▶ Deploy
     └──▶ Frontend Build & Lint ──▶ Docker Push Frontend ────┘
```

**Triggers:** `push` to main/develop · `pull_request` to main · Schedule (Mon 06:00 UTC) · `workflow_dispatch`

---

### 2️⃣ Security & Code Quality

| Workflow | File | Triggers | What it does |
|----------|------|----------|-------------|
| **CodeQL Analysis** | `codeql.yml` | Push, PR, Schedule (Wed 04:00 UTC), Manual | Scans Java (backend) and JavaScript/TypeScript (frontend) for vulnerabilities using GitHub CodeQL `security-and-quality` queries |
| **Copilot Code Review** | `copilot-review.yml` | PR, Manual | AI-powered review — GitHub Copilot automatically comments on pull requests with suggestions and issues |

```
PR Opened ──▶ CodeQL Scan (Java + JS/TS)
         └──▶ Copilot Code Review
```

---

### 3️⃣ Dependency Management

| Workflow | File | Triggers | What it does |
|----------|------|----------|-------------|
| **Dependabot Updates** | `dependabot.yml` | Weekly (Mon 06:00 UTC) | Creates PRs for outdated Maven, npm, GitHub Actions, and Docker base-image dependencies |
| **Dependabot Auto-Merge** | `dependabot-auto-merge.yml` | Dependabot PR, Manual | Auto-approves & squash-merges **minor/patch** updates; flags **major** updates for manual review |
| **Dependency Audit** | `dependency-updates.yml` | Schedule (Mon 07:00 UTC), Manual | Runs OWASP dependency-check (backend) and `npm audit` (frontend); uploads reports as artifacts |

```
Dependabot PR (minor/patch) ──▶ Auto-Approve ──▶ Auto-Merge (squash)
Dependabot PR (major)       ──▶ Comment "⚠️ Manual review required"
```

---

### 4️⃣ Release & Publish

| Workflow | File | Triggers | What it does |
|----------|------|----------|-------------|
| **Release** | `release.yml` | Tag push `v*.*.*`, Manual | Builds backend JAR + frontend bundle, creates GitHub Release with auto-generated notes and attached artifacts |
| **Publish Packages** | `publish-packages.yml` | Release, Manual | Publishes Maven artifact to GitHub Packages; pushes versioned backend & frontend Docker images to Docker Hub |

```
Tag v*.*.* ──▶ Build Artifacts (JAR + Frontend) ──▶ GitHub Release
Release Published ──▶ Maven → GitHub Packages
                  ──▶ Backend Image → Docker Hub
                  ──▶ Frontend Image → Docker Hub
```

---

### 5️⃣ GitHub Marketplace (`publish-marketplace.yml` + `action.yml`)

The repository includes a **composite action** (`action.yml`) that deploys the full stack via Docker Compose. The `publish-marketplace.yml` workflow validates and publishes it to the GitHub Marketplace on each release.

**Usage in other repositories:**
```yaml
- name: Deploy Task Management Platform
  uses: <OWNER>/<REPO>@v1
  with:
    database-password: ${{ secrets.DB_PASSWORD }}
    jwt-secret: ${{ secrets.JWT_SECRET }}
    api-url: "http://myapp.example.com:8080/api"
```

**Triggers:** Release published · `workflow_dispatch`

---

### Manual Trigger (workflow_dispatch)

All 9 workflows can be triggered manually from the **Actions** tab:

1. Go to **Actions** → select the workflow
2. Click **"Run workflow"**
3. Fill in optional inputs (environment, version, etc.)
4. Click **"Run workflow"**

### Scheduled Runs

| Schedule | Workflows |
|----------|-----------|
| **Monday 06:00 UTC** | CI/CD Pipeline · Dependabot Updates |
| **Monday 07:00 UTC** | Dependency Audit (OWASP + npm audit) |
| **Wednesday 04:00 UTC** | CodeQL Security Analysis |

### Required Secrets

Configure these in **GitHub → Settings → Secrets and variables → Actions**:

| Secret | Used By | Description |
|--------|---------|-------------|
| `GITHUB_TOKEN` | All workflows | Automatically provided by GitHub Actions |
| `DOCKERHUB_USERNAME` | CI/CD, Publish Packages | Your Docker Hub username |
| `DOCKERHUB_PASSWORD` | CI/CD, Publish Packages | Docker Hub password or [access token](https://hub.docker.com/settings/security) _(recommended)_ |
| `DB_PASSWORD` | Marketplace action | PostgreSQL database password |
| `JWT_SECRET` | Marketplace action | JWT signing secret |

---

## Database Schema

### `users` table
| Column       | Type          | Constraints          |
|--------------|---------------|----------------------|
| `id`         | BIGSERIAL     | PRIMARY KEY          |
| `username`   | VARCHAR(50)   | UNIQUE, NOT NULL     |
| `email`      | VARCHAR(100)  | UNIQUE, NOT NULL     |
| `password`   | VARCHAR(255)  | NOT NULL (BCrypt)    |
| `role`       | VARCHAR(20)   | NOT NULL (USER/ADMIN)|
| `created_at` | TIMESTAMP     | DEFAULT NOW()        |

### `tasks` table
| Column        | Type         | Constraints                        |
|---------------|--------------|------------------------------------|
| `id`          | BIGSERIAL    | PRIMARY KEY                        |
| `title`       | VARCHAR(200) | NOT NULL                           |
| `description` | TEXT         | NULLABLE                           |
| `status`      | VARCHAR(20)  | NOT NULL (TODO/IN_PROGRESS/DONE)   |
| `priority`    | VARCHAR(10)  | NOT NULL (LOW/MEDIUM/HIGH)         |
| `due_date`    | DATE         | NULLABLE                           |
| `created_at`  | TIMESTAMP    | DEFAULT NOW()                      |
| `updated_at`  | TIMESTAMP    | AUTO-UPDATED                       |
| `owner_id`    | BIGINT       | FK → users(id), NOT NULL           |

---

## API Documentation

### Authentication

| Method | Endpoint             | Auth Required | Description            |
|--------|----------------------|---------------|------------------------|
| POST   | `/api/auth/register` | ❌ Public     | Register new user      |
| POST   | `/api/auth/login`    | ❌ Public     | Login, get JWT token   |

**Register request body:**
```json
{ "username": "john", "email": "john@example.com", "password": "secret123" }
```

**Login request body:**
```json
{ "username": "john", "password": "secret123" }
```

**Auth response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "john",
  "email": "john@example.com",
  "role": "USER"
}
```

---

### Tasks (require `Authorization: Bearer <token>`)

| Method | Endpoint                  | ADMIN | USER      | Description                        |
|--------|---------------------------|-------|-----------|------------------------------------|
| GET    | `/api/tasks`              | All   | Own only  | Paginated task list with filters   |
| POST   | `/api/tasks`              | ✅    | ✅        | Create new task                    |
| GET    | `/api/tasks/{id}`         | ✅    | Own only  | Get task by ID                     |
| PUT    | `/api/tasks/{id}`         | ✅    | Own only  | Update task (full)                 |
| PATCH  | `/api/tasks/{id}/status`  | ✅    | Own only  | Update task status                 |
| DELETE | `/api/tasks/{id}`         | ✅    | Own only  | Delete task                        |

**GET /api/tasks query parameters:**
| Param       | Type    | Default     | Values                         |
|-------------|---------|-------------|--------------------------------|
| `status`    | string  | (none)      | `TODO`, `IN_PROGRESS`, `DONE`  |
| `priority`  | string  | (none)      | `LOW`, `MEDIUM`, `HIGH`        |
| `page`      | integer | `0`         | 0-based page number            |
| `size`      | integer | `10`        | Items per page                 |
| `sortBy`    | string  | `createdAt` | `createdAt`, `dueDate`, `priority` |
| `direction` | string  | `desc`      | `asc`, `desc`                  |

**Create/Update task request body:**
```json
{
  "title": "Fix login bug",
  "description": "The login form fails on mobile Safari",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-04-01"
}
```

---

### Users (ADMIN only)

| Method | Endpoint      | Auth Required | Description       |
|--------|---------------|---------------|-------------------|
| GET    | `/api/users`  | ✅ ADMIN      | List all users    |

---

### Interactive Swagger UI
Visit `http://localhost:8080/api/swagger-ui.html` after starting the backend.
Use the **Authorize** button to enter your JWT token (`Bearer <token>`).

---

## Setup Instructions

### Prerequisites
- **Docker & Docker Compose** (recommended) — OR
- **Java 21**, **Maven 3.9+**, **Node.js 20+**, **PostgreSQL 16**

---

### Option 1: Docker Compose (Recommended)

```bash
# 1. Clone / navigate to the project
cd task_management_platform

# 2. Start all services (db + backend + frontend)
docker compose up --build

# Services will be available at:
#   Frontend:  http://localhost:3000
#   Backend:   http://localhost:8080/api
#   Swagger:   http://localhost:8080/api/swagger-ui.html
#   PostgreSQL: localhost:5432
```

To stop:
```bash
docker compose down
# To also remove volumes (database data):
docker compose down -v
```

---

### Option 2: Local Development

#### 1. Database Setup

```sql
-- Run in psql
CREATE DATABASE taskmanager;
CREATE USER taskuser WITH PASSWORD 'taskpassword';
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO taskuser;
```

#### 2. Backend

```bash
cd backend

# Configure environment (or set variables in application.yml)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/taskmanager
export SPRING_DATASOURCE_USERNAME=taskuser
export SPRING_DATASOURCE_PASSWORD=<your-database-password>
export JWT_SECRET=$(openssl rand -base64 32)

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run

# Backend starts on http://localhost:8080
```

#### 3. Frontend

```bash
cd frontend

# Install dependencies
npm install

# Create environment file
echo "NEXT_PUBLIC_API_URL=http://localhost:8080/api" > .env.local

# Start dev server
npm run dev

# Frontend starts on http://localhost:3000
```

---

## Database Configuration

The backend uses **Hibernate DDL auto-update**, so all tables are created automatically on first run. No manual schema scripts are needed.

To use a different database, update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:<port>/<database>
    username: <username>
    password: <password>
```

---

## Default Admin Account

A default admin account is **seeded automatically** on first startup:

| Field    | Default Value           |
|----------|-------------------------|
| Username | `admin`                 |
| Password | `admin123`              |
| Email    | `admin@taskmanager.com` |

> ⚠️ **Change the default admin password immediately** in any non-local environment.

You can customize the admin credentials via environment variables before first startup:
```bash
export ADMIN_USERNAME=myadmin
export ADMIN_EMAIL=myadmin@company.com
export ADMIN_PASSWORD=strong_password_here
```

If a user named `admin` already exists with the `USER` role, it will be automatically promoted to `ADMIN` on the next startup.

---

## Environment Variables Reference

| Variable                    | Default                          | Description                    |
|-----------------------------|----------------------------------|--------------------------------|
| `SPRING_DATASOURCE_URL`     | `jdbc:postgresql://...`         | PostgreSQL JDBC URL            |
| `SPRING_DATASOURCE_USERNAME`| `taskuser`                       | Database username              |
| `SPRING_DATASOURCE_PASSWORD`| `taskpassword`                   | Database password              |
| `JWT_SECRET`                | (see .env.example)               | Base64 HMAC-SHA256 secret key  |
| `JWT_EXPIRATION`            | `86400000` (24h)                 | Token expiry in milliseconds   |
| `NEXT_PUBLIC_API_URL`       | `http://localhost:8080/api`      | Backend API base URL           |

---

## IntelliJ IDEA Setup

To get full IDE support for the backend in IntelliJ:

1. Open IntelliJ IDEA
2. Go to **File → Open** and select `backend/pom.xml`
3. Choose **"Open as Project"** — IntelliJ will import the Maven project and configure all source roots
4. Alternatively, right-click `backend/pom.xml` in the Project panel → **"Add as Maven Project"**

This resolves the "Java file is located outside of the module source root" warning and enables full code completion, error highlighting, and run configurations.

---

## Technology Stack

### Backend
- **Spring Boot 3.2** — Application framework
- **Spring Security 6** — JWT authentication & RBAC
- **Spring Data JPA / Hibernate** — ORM & database layer
- **PostgreSQL** — Relational database
- **JJWT 0.11.5** — JWT token generation & validation
- **SpringDoc OpenAPI 2.3** — Swagger UI & API docs
- **Bean Validation** — Input validation

### Frontend
- **Next.js 14** (App Router) — React framework
- **TypeScript** — Type safety
- **Tailwind CSS** — Utility-first styling
- **Axios** — HTTP client with interceptors
- **React Hot Toast** — Toast notifications
- **Lucide React** — Icon library
- **date-fns** — Date formatting

### DevOps
- **Docker** — Containerization
- **Docker Compose** — Multi-service orchestration
- **GitHub Actions** — CI/CD pipelines
- **Docker Hub** — Docker image hosting (backend & frontend pushed separately)
- **GitHub Packages** — Maven artifact hosting
- **CodeQL** — Static security analysis
- **Dependabot** — Automated dependency updates
- **GitHub Copilot** — AI-powered code review

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -m "feat: add my feature"`
4. Push to branch: `git push origin feature/my-feature`
5. Open a Pull Request — CI/CD and Copilot review will run automatically

---

## License

This project is open source and available under the [MIT License](LICENSE).
