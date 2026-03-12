# Setup Guide

This guide covers everything needed to install, configure, and run the Task Management Platform on a local development machine.

---

## Prerequisites

Choose **one** of the two setup options below.

### Option A — Docker (Recommended)

| Requirement       | Minimum Version | Verify Command             |
|-------------------|-----------------|----------------------------|
| Docker Engine     | 20.10+          | `docker --version`         |
| Docker Compose    | 2.0+            | `docker compose version`   |

### Option B — Local Development

| Requirement   | Minimum Version | Verify Command         |
|---------------|-----------------|------------------------|
| Java JDK      | 21 (LTS)        | `java --version`       |
| Apache Maven   | 3.9+            | `mvn --version`        |
| Node.js        | 20 (LTS)        | `node --version`       |
| npm             | 10+             | `npm --version`        |
| PostgreSQL      | 16              | `psql --version`       |

---

## Option A: Docker Compose (One-Command Setup)

This is the fastest way to get the full stack running. Docker Compose starts PostgreSQL, the Spring Boot backend, and the Next.js frontend as three interconnected containers.

### 1. Clone the Repository

```bash
git clone https://github.com/<OWNER>/task_management_platform.git
cd task_management_platform
```

### 2. Start All Services

```bash
docker compose up --build
```

The first build takes several minutes while Docker downloads base images and compiles the application. Subsequent starts are much faster due to layer caching.

### 3. Verify the Services

Once all three containers are healthy, the following services are available:

| Service        | URL                                        |
|----------------|--------------------------------------------|
| Frontend       | http://localhost:3000                       |
| Backend API    | http://localhost:8080/api                   |
| Swagger UI     | http://localhost:8080/api/swagger-ui.html   |
| PostgreSQL     | `localhost:5432` (user: `taskuser` / pass: `taskpassword`) |

### 4. Stop the Services

```bash
# Stop containers (data is preserved in the Docker volume)
docker compose down

# Stop containers AND delete the database volume
docker compose down -v
```

### Customizing Docker Configuration

To override the default credentials or ports, create a `.env` file in the project root (see `.env.example`):

```dotenv
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/taskmanager
SPRING_DATASOURCE_USERNAME=taskuser
SPRING_DATASOURCE_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_64_char_hex_secret_key_here
JWT_EXPIRATION=86400000

# Frontend
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

---

## Option B: Local Development

### Step 1 — Database Setup

Start PostgreSQL and create the database and user:

```bash
# Connect to PostgreSQL as superuser
psql -U postgres
```

```sql
CREATE DATABASE taskmanager;
CREATE USER taskuser WITH PASSWORD 'taskpassword';
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO taskuser;

-- Grant schema privileges (PostgreSQL 15+)
\c taskmanager
GRANT ALL ON SCHEMA public TO taskuser;
```

> **Note:** Hibernate automatically creates all tables on first startup (`ddl-auto: update`). No manual schema scripts are needed.

### Step 2 — Backend

```bash
cd backend
```

Set the required environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/taskmanager
export SPRING_DATASOURCE_USERNAME=taskuser
export SPRING_DATASOURCE_PASSWORD=<your-database-password>
export JWT_SECRET=$(openssl rand -hex 32)
export JWT_EXPIRATION=86400000
```

Build and run:

```bash
# Compile and package (skip tests for faster startup)
mvn clean install -DskipTests

# Start the application
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**. Verify it's running:

```bash
curl http://localhost:8080/api/v3/api-docs | head -c 200
```

### Step 3 — Frontend

Open a **new terminal** and navigate to the frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Create the environment configuration:

```bash
echo "NEXT_PUBLIC_API_URL=http://localhost:8080/api" > .env.local
```

Start the development server:

```bash
npm run dev
```

The frontend starts on **http://localhost:3000** with hot-reload enabled.

---

## First-Time Usage

### 1. Register an Account

Open http://localhost:3000 in your browser. You will be redirected to the login page. Click **Register** to create a new account.

Alternatively, use the API directly:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"secret123"}'
```

### 2. Create Your First Task

After logging in, use the **Create Task** button on the tasks page to add a new task with a title, description, status, priority, and optional due date.

### 3. Promote a User to Admin (Optional)

New registrations default to the `USER` role. To grant admin privileges, update the database directly:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'johndoe';
```

Log out and log back in for the role change to take effect. Admins can see and manage all users' tasks.

---

## Environment Variables Reference

| Variable                     | Default                                                              | Description                        |
|------------------------------|----------------------------------------------------------------------|------------------------------------|
| `SPRING_DATASOURCE_URL`     | `jdbc:postgresql://localhost:5432/taskmanager`                       | PostgreSQL JDBC connection URL     |
| `SPRING_DATASOURCE_USERNAME`| `taskuser`                                                           | Database username                  |
| `SPRING_DATASOURCE_PASSWORD`| `taskpassword`                                                       | Database password                  |
| `JWT_SECRET`                | `404E635266...` (64-char hex)                                        | HMAC-SHA256 signing key for JWTs   |
| `JWT_EXPIRATION`            | `86400000` (24 hours in ms)                                          | JWT token lifetime in milliseconds |
| `NEXT_PUBLIC_API_URL`       | `http://localhost:8080/api`                                          | Backend API URL used by the frontend |

> ⚠️ **Important:** Change `JWT_SECRET` and `SPRING_DATASOURCE_PASSWORD` to strong, unique values for any non-local environment.

---

## IDE Setup

### IntelliJ IDEA

1. Open IntelliJ IDEA → **File → Open** → select `backend/pom.xml`
2. Choose **"Open as Project"**
3. IntelliJ imports the Maven project and configures all source roots automatically
4. If opening the root folder instead, right-click `backend/pom.xml` → **"Add as Maven Project"**

This resolves the "Java file is located outside of the module source root" warning and enables full code completion.

### VS Code

1. Open the root `task_management_platform` folder in VS Code
2. Install recommended extensions: **Extension Pack for Java**, **Spring Boot Extension Pack**, **ES7+ React Snippets**, **Tailwind CSS IntelliSense**
3. The backend can be run via the Spring Boot Dashboard or terminal
4. The frontend can be run with `npm run dev` from the integrated terminal

---

## Troubleshooting

| Problem                                    | Solution                                                                                                  |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| Port 5432 already in use                   | Stop any local PostgreSQL service: `brew services stop postgresql` (macOS) or change the port in `docker-compose.yml` |
| Port 8080 already in use                   | Stop the conflicting process or set `server.port` in `application.yml`                                    |
| Port 3000 already in use                   | Stop the conflicting process or run `npm run dev -- -p 3001`                                              |
| Backend can't connect to database          | Ensure PostgreSQL is running and credentials match `application.yml` or environment variables              |
| Frontend shows "Network Error"             | Ensure the backend is running and `NEXT_PUBLIC_API_URL` points to the correct backend URL                 |
| `docker compose up` fails on M1/M2 Mac    | Ensure Docker Desktop is updated; `postgres:16-alpine` supports ARM64 natively                            |
| Hibernate schema errors after model change | Stop the app, drop and recreate the database, then restart — Hibernate will regenerate tables              |
| JWT token expired / 401 errors             | Log in again to obtain a fresh token; default expiration is 24 hours                                      |

---

## Related Documentation

| Document                                      | Description                                    |
|-----------------------------------------------|------------------------------------------------|
| [Project Overview](./PROJECT_OVERVIEW.md)     | Architecture, tech stack, and features         |
| [API Reference](./API_REFERENCE.md)           | Complete REST API endpoint documentation       |
| [Deployment & CI/CD](./DEPLOYMENT.md)         | Pipelines, Docker, and production deployment   |

