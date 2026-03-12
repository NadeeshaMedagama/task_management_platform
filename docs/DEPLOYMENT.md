# Deployment & CI/CD

This document covers the containerization strategy, CI/CD pipeline architecture, release process, and production deployment considerations for the Task Management Platform.

---

## Table of Contents

1. [Docker Architecture](#docker-architecture)
2. [CI/CD Pipeline Overview](#cicd-pipeline-overview)
3. [Workflow Details](#workflow-details)
4. [Release & Publishing Process](#release--publishing-process)
5. [GitHub Marketplace Action](#github-marketplace-action)
6. [Required Secrets](#required-secrets)
7. [Production Deployment Considerations](#production-deployment-considerations)

---

## Docker Architecture

Both the backend and frontend use **multi-stage Docker builds** to produce minimal, secure production images.

### Backend Image (`backend/Dockerfile`)

```
┌─────────────────────────────────────┐
│  Stage 1: Build                     │
│  maven:3.9-eclipse-temurin-21-alpine│
│  ─ Resolve dependencies (cached)    │
│  ─ Compile & package (mvn package)  │
│  ─ Output: app.jar                  │
├─────────────────────────────────────┤
│  Stage 2: Runtime                   │
│  eclipse-temurin:21-jre-alpine      │
│  ─ Non-root user (appuser)          │
│  ─ Copy app.jar from build stage    │
│  ─ Expose port 8080                 │
└─────────────────────────────────────┘
```

- **Base image:** Eclipse Temurin JRE 21 (Alpine) — minimal footprint
- **Security:** Runs as non-root `appuser`
- **Dependency caching:** `pom.xml` is copied before source code so Maven dependencies are cached in a separate Docker layer

### Frontend Image (`frontend/Dockerfile`)

```
┌──────────────────────────────────────┐
│  Stage 1: Build                      │
│  node:20-alpine                      │
│  ─ Install npm dependencies (npm ci) │
│  ─ Build Next.js production bundle   │
│  ─ Output: .next/standalone          │
├──────────────────────────────────────┤
│  Stage 2: Runtime                    │
│  node:20-alpine                      │
│  ─ Non-root user (nextjs:nodejs)     │
│  ─ Copy standalone + static assets   │
│  ─ Expose port 3000                  │
└──────────────────────────────────────┘
```

- **Base image:** Node.js 20 (Alpine)
- **Security:** Runs as non-root `nextjs` user
- **Standalone output:** Next.js produces a self-contained `server.js` — no `node_modules` needed at runtime

### Docker Compose Services

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│    db    │◀────│ backend  │◀────│ frontend │
│ :5432    │     │ :8080    │     │ :3000    │
│ postgres │     │ Spring   │     │ Next.js  │
└──────────┘     └──────────┘     └──────────┘
      ▲
      │ healthcheck (pg_isready)
      │ depends_on: service_healthy
```

| Container              | Image                       | Port | Network   |
|------------------------|-----------------------------|------|-----------|
| `taskmanager_db`       | `postgres:16-alpine`        | 5432 | `task-net`|
| `taskmanager_backend`  | Built from `backend/`       | 8080 | `task-net`|
| `taskmanager_frontend` | Built from `frontend/`      | 3000 | `task-net`|

**Startup order:** The backend waits for the database healthcheck to pass (`pg_isready`) before starting. The frontend depends on the backend service.

---

## CI/CD Pipeline Overview

The project uses **8 GitHub Actions workflows** covering 5 functional areas:

```
┌─────────────────────────────────────────────────────────────────┐
│                        GitHub Actions                           │
├──────────────────┬──────────────────┬───────────────────────────┤
│  Build & Deploy  │  Security        │  Dependencies             │
│  ─ ci-cd.yml     │  ─ codeql.yml    │  ─ dependabot.yml         │
│                  │  ─ copilot-review │  ─ dependabot-auto-merge  │
│                  │                  │  ─ dependency-updates      │
├──────────────────┴──────────────────┴───────────────────────────┤
│  Release & Publish                                              │
│  ─ release.yml                                                  │
│  ─ publish-packages.yml                                         │
│  ─ publish-marketplace.yml                                      │
└─────────────────────────────────────────────────────────────────┘
```

### Trigger Summary

| Workflow                 | Push | PR | Schedule          | Release | Manual |
|--------------------------|------|----|-------------------|---------|--------|
| CI/CD Pipeline           | ✅   | ✅ | Mon 06:00 UTC     | —       | ✅     |
| CodeQL Analysis          | ✅   | ✅ | Wed 04:00 UTC     | —       | ✅     |
| Copilot Code Review      | —    | ✅ | —                 | —       | ✅     |
| Dependabot Auto-Merge    | —    | ✅ | —                 | —       | ✅     |
| Dependency Audit         | —    | —  | Mon 07:00 UTC     | —       | ✅     |
| Release                  | —    | —  | —                 | Tag `v*`| ✅     |
| Publish Packages         | —    | —  | —                 | ✅      | ✅     |
| Publish Marketplace      | —    | —  | —                 | ✅      | ✅     |

---

## Workflow Details

### 1. Build, Test & Deploy (`ci-cd.yml`)

The core pipeline that validates code quality and delivers container images.

```
Push / PR ──▶ Backend Build & Test ──▶ Docker Push Backend ──┐
     │                                                        ├──▶ Deploy
     └──▶ Frontend Build & Lint ──▶ Docker Push Frontend ────┘
```

| Stage                    | Actions                                                                |
|--------------------------|------------------------------------------------------------------------|
| **Backend Build & Test** | Maven compile + unit tests with PostgreSQL 16 service container        |
| **Frontend Build & Lint**| `npm ci` → ESLint → `npm run build`                                   |
| **Docker Push Backend**  | Build image → push to Docker Hub (`<user>/task-management-backend`)    |
| **Docker Push Frontend** | Build image → push to Docker Hub (`<user>/task-management-frontend`)   |
| **Deploy**               | Runs after both images are pushed (customizable deployment step)       |

Backend and frontend Docker images are **built and pushed in parallel**.

### 2. CodeQL Analysis (`codeql.yml`)

Scans both **Java** (backend) and **JavaScript/TypeScript** (frontend) using GitHub's CodeQL `security-and-quality` query suite. Runs on every push, PR, and weekly schedule.

### 3. Copilot Code Review (`copilot-review.yml`)

GitHub Copilot automatically reviews pull requests, posting inline comments with code quality suggestions and potential issues.

### 4. Dependabot Configuration (`dependabot.yml`)

Monitors four dependency ecosystems:

| Ecosystem     | Path        | Schedule    |
|---------------|-------------|-------------|
| Maven         | `/backend`  | Weekly (Mon)|
| npm           | `/frontend` | Weekly (Mon)|
| GitHub Actions| `/`         | Weekly (Mon)|
| Docker        | `/`         | Weekly (Mon)|

### 5. Dependabot Auto-Merge (`dependabot-auto-merge.yml`)

- **Minor/Patch** updates: automatically approved and squash-merged
- **Major** updates: flagged with a comment requesting manual review

### 6. Dependency Audit (`dependency-updates.yml`)

Runs OWASP Dependency-Check (backend) and `npm audit` (frontend). Reports are uploaded as workflow artifacts for review.

---

## Release & Publishing Process

### Creating a Release

Releases are triggered by pushing a semantic version tag:

```bash
# Tag the release
git tag v1.0.0
git push origin v1.0.0
```

### Release Workflow (`release.yml`)

1. Builds the backend JAR artifact (`task-management-backend-1.0.0.jar`)
2. Builds the frontend production bundle
3. Creates a **GitHub Release** with auto-generated release notes
4. Attaches build artifacts to the release

### Publish Packages Workflow (`publish-packages.yml`)

Triggered automatically when a GitHub Release is published:

1. **Maven Artifact** → published to **GitHub Packages**
2. **Backend Docker Image** → tagged and pushed to **Docker Hub**
3. **Frontend Docker Image** → tagged and pushed to **Docker Hub**

```
Release v1.0.0 Published
    ├── Maven JAR → GitHub Packages (com.taskmanager:task-management-backend:1.0.0)
    ├── Backend → Docker Hub (<user>/task-management-backend:1.0.0)
    └── Frontend → Docker Hub (<user>/task-management-frontend:1.0.0)
```

---

## GitHub Marketplace Action

The repository includes a **composite action** (`action.yml`) that deploys the full stack using Docker Compose. The `publish-marketplace.yml` workflow publishes it to the GitHub Marketplace on each release.

### Usage in External Repositories

```yaml
- name: Deploy Task Management Platform
  uses: <OWNER>/<REPO>@v1
  with:
    database-password: ${{ secrets.DB_PASSWORD }}
    jwt-secret: ${{ secrets.JWT_SECRET }}
    api-url: "http://myapp.example.com:8080/api"
```

### Action Inputs

| Input               | Required | Default                                  | Description                     |
|---------------------|----------|------------------------------------------|---------------------------------|
| `database-url`      | ❌       | `jdbc:postgresql://db:5432/taskmanager`  | PostgreSQL JDBC URL             |
| `database-username` | ❌       | `taskuser`                               | Database username               |
| `database-password` | ✅       | —                                        | Database password               |
| `jwt-secret`        | ✅       | —                                        | JWT signing secret (64+ hex)    |
| `jwt-expiration`    | ❌       | `86400000`                               | Token lifetime in milliseconds  |
| `api-url`           | ❌       | `http://localhost:8080/api`              | Public backend API URL          |
| `backend-port`      | ❌       | `8080`                                   | Backend exposed port            |
| `frontend-port`     | ❌       | `3000`                                   | Frontend exposed port           |

---

## Required Secrets

Configure in **GitHub → Settings → Secrets and variables → Actions**:

| Secret               | Used By                   | Description                                          |
|----------------------|---------------------------|------------------------------------------------------|
| `GITHUB_TOKEN`       | All workflows             | Automatically provided by GitHub Actions             |
| `DOCKERHUB_USERNAME` | CI/CD, Publish Packages   | Docker Hub username                                  |
| `DOCKERHUB_PASSWORD` | CI/CD, Publish Packages   | Docker Hub access token ([recommended](https://hub.docker.com/settings/security)) |
| `DB_PASSWORD`        | Marketplace action        | PostgreSQL database password                         |
| `JWT_SECRET`         | Marketplace action        | JWT signing secret                                   |

---

## Production Deployment Considerations

### Security Checklist

| Area                | Recommendation                                                                                     |
|---------------------|----------------------------------------------------------------------------------------------------|
| **JWT Secret**      | Generate a cryptographically strong secret (min 64 hex characters); never commit to source control |
| **Database**        | Use a managed PostgreSQL service (AWS RDS, GCP Cloud SQL); enforce SSL connections                 |
| **Passwords**       | Store all secrets in a vault (GitHub Secrets, AWS Secrets Manager, HashiCorp Vault)                |
| **HTTPS**           | Terminate TLS at a reverse proxy (Nginx, Traefik, or cloud load balancer)                         |
| **CORS**            | Restrict allowed origins in `SecurityConfig` to your production frontend domain                   |
| **Non-root users**  | Both Dockerfiles already run as non-root — no changes needed                                      |
| **DDL Auto**        | Set `ddl-auto: validate` in production to prevent accidental schema changes                       |

### Recommended Production Architecture

```
┌──────────┐     ┌────────────────┐     ┌─────────────┐     ┌──────────────┐
│  Client  │────▶│  Load Balancer │────▶│  Backend ×N │────▶│  PostgreSQL  │
│ (Browser)│     │  (TLS + CORS)  │     │  (Stateless)│     │  (Managed)   │
└──────────┘     └────────┬───────┘     └─────────────┘     └──────────────┘
                          │
                          ▼
                 ┌────────────────┐
                 │  Frontend CDN  │
                 │  (Next.js SSR) │
                 └────────────────┘
```

### Environment Variables for Production

```bash
# Database — use a managed service with SSL
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db.example.com:5432/taskmanager?sslmode=require
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=<strong-password>

# JWT — generate a strong unique secret
JWT_SECRET=<64-character-hex-string>
JWT_EXPIRATION=3600000   # Consider shorter expiration (1 hour)

# Hibernate — validate only, never auto-update in production
SPRING_JPA_HIBERNATE_DDL_AUTO=validate

# Frontend
NEXT_PUBLIC_API_URL=https://api.yourdomain.com
```

### Scaling

- The backend is **stateless** (JWT-based auth, no server sessions) and can be horizontally scaled behind a load balancer
- The frontend uses Next.js standalone mode, also suitable for horizontal scaling or CDN deployment
- The database is the single shared state — use connection pooling (HikariCP is included by default) and consider read replicas for high-read workloads

### Manual Trigger

All workflows support `workflow_dispatch` for on-demand execution:

1. Navigate to **Actions** tab in GitHub
2. Select the desired workflow
3. Click **Run workflow**
4. Provide any required inputs and confirm

---

## Scheduled Automation Summary

| Schedule              | Workflow                                    |
|-----------------------|---------------------------------------------|
| **Monday 06:00 UTC**  | CI/CD Pipeline, Dependabot Updates          |
| **Monday 07:00 UTC**  | Dependency Audit (OWASP + npm audit)        |
| **Wednesday 04:00 UTC** | CodeQL Security Analysis                  |

---

## Related Documentation

| Document                                      | Description                                    |
|-----------------------------------------------|------------------------------------------------|
| [Project Overview](./PROJECT_OVERVIEW.md)     | Architecture, tech stack, and features         |
| [Setup Guide](./SETUP_GUIDE.md)              | Installation and local development             |
| [API Reference](./API_REFERENCE.md)           | Complete REST API endpoint documentation       |
| [Detailed API Docs](../API_DOCUMENTATION.md)  | Extended API documentation with full examples  |

