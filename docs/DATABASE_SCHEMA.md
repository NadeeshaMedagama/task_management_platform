# Database Schema Reference

> **Version:** 1.0.0
> **Last Updated:** March 12, 2026
> **Database Engine:** PostgreSQL 16

---

## Table of Contents

- [Overview](#overview)
- [Entity-Relationship Diagram](#entity-relationship-diagram)
- [Tables](#tables)
  - [`users`](#users)
  - [`tasks`](#tasks)
- [Enumerated Types](#enumerated-types)
  - [Role](#role)
  - [TaskStatus](#taskstatus)
  - [Priority](#priority)
- [Relationships](#relationships)
- [Indexes and Constraints](#indexes-and-constraints)
- [Schema Management](#schema-management)
- [Environment Configuration](#environment-configuration)
- [Testing Configuration](#testing-configuration)

---

## Overview

The Task Management Platform persists data in a **PostgreSQL 16** relational database. The schema is managed automatically by **Hibernate ORM** (via Spring Data JPA) using the `ddl-auto: update` strategy, which applies incremental schema changes at application startup.

The data model consists of two core entities — **Users** and **Tasks** — connected by a one-to-many ownership relationship. Role-based access control is implemented through a `role` column on the `users` table, and task lifecycle state is tracked via `status` and `priority` enumerations stored as strings.

---

## Entity-Relationship Diagram

```
┌─────────────────────────────────────┐       ┌──────────────────────────────────────────────┐
│              users                  │       │                   tasks                      │
├─────────────────────────────────────┤       ├──────────────────────────────────────────────┤
│ PK  id          BIGSERIAL           │       │ PK  id            BIGSERIAL                  │
│     username    VARCHAR(50)  UQ NN  │       │     title         VARCHAR(200)         NN    │
│     email       VARCHAR(100) UQ NN  │       │     description   TEXT                       │
│     password    VARCHAR(255)    NN  │       │     status        VARCHAR(20)          NN    │
│     role        VARCHAR(20)     NN  │       │     priority      VARCHAR(10)          NN    │
│     created_at  TIMESTAMP           │       │     due_date      DATE                       │
│                                     │       │     created_at    TIMESTAMP                  │
│                                     │◀──┐   │     updated_at    TIMESTAMP                  │
│                                     │   │   │ FK  owner_id      BIGINT               NN   │
└─────────────────────────────────────┘   │   └──────────────────────────────────────────────┘
                                          │                        │
                                          └────────────────────────┘
                                              owner_id → users.id
                                              (Many-to-One)
```

**Legend:** `PK` = Primary Key | `FK` = Foreign Key | `UQ` = Unique | `NN` = Not Null

---

## Tables

### `users`

Stores registered user accounts. Implements Spring Security's `UserDetails` interface for authentication and authorization.

| Column       | Data Type        | Nullable | Default         | Constraints          | Description                                      |
|--------------|------------------|----------|-----------------|----------------------|--------------------------------------------------|
| `id`         | `BIGSERIAL`      | No       | Auto-generated  | `PRIMARY KEY`        | Unique identifier for the user.                  |
| `username`   | `VARCHAR(50)`    | No       | —               | `UNIQUE`, `NOT NULL` | Login handle; must be unique across all users.    |
| `email`      | `VARCHAR(100)`   | No       | —               | `UNIQUE`, `NOT NULL` | Email address; must be unique across all users.   |
| `password`   | `VARCHAR(255)`   | No       | —               | `NOT NULL`           | BCrypt-hashed password. Never stored in plaintext.|
| `role`       | `VARCHAR(20)`    | No       | —               | `NOT NULL`           | Authorization role. See [Role](#role) enum.       |
| `created_at` | `TIMESTAMP`      | Yes      | Current time     | Non-updatable        | Record creation timestamp; set automatically by `@PrePersist`. |

**Notes:**
- The `password` column stores a BCrypt hash, not the raw password.
- The `created_at` column is set once at insert time and is never updated thereafter.
- Spring Security authorities are derived at runtime as `ROLE_<role>` (e.g., `ROLE_ADMIN`).

---

### `tasks`

Stores task records. Each task is owned by exactly one user and tracks its lifecycle through status and priority enumerations.

| Column        | Data Type        | Nullable | Default         | Constraints                          | Description                                              |
|---------------|------------------|----------|-----------------|--------------------------------------|----------------------------------------------------------|
| `id`          | `BIGSERIAL`      | No       | Auto-generated  | `PRIMARY KEY`                        | Unique identifier for the task.                          |
| `title`       | `VARCHAR(200)`   | No       | —               | `NOT NULL`                           | Short summary of the task.                               |
| `description` | `TEXT`           | Yes      | `NULL`          | —                                    | Detailed description or acceptance criteria.             |
| `status`      | `VARCHAR(20)`    | No       | `TODO`          | `NOT NULL`                           | Current lifecycle state. See [TaskStatus](#taskstatus).  |
| `priority`    | `VARCHAR(10)`    | No       | —               | `NOT NULL`                           | Urgency level. See [Priority](#priority).                |
| `due_date`    | `DATE`           | Yes      | `NULL`          | —                                    | Optional deadline for task completion.                   |
| `created_at`  | `TIMESTAMP`      | Yes      | Current time     | Non-updatable                        | Record creation timestamp; set by `@PrePersist`.         |
| `updated_at`  | `TIMESTAMP`      | Yes      | Current time     | Auto-updated                         | Last modification timestamp; set by `@PreUpdate`.        |
| `owner_id`    | `BIGINT`         | No       | —               | `NOT NULL`, `FOREIGN KEY → users.id` | The user who owns this task.                             |

**Notes:**
- When a task is first persisted and no `status` is provided, it defaults to `TODO` via the `@PrePersist` lifecycle callback.
- `created_at` is set once at insert; `updated_at` is refreshed on every update.
- The `owner_id` foreign key uses `FetchType.EAGER`, so the owning `User` entity is always loaded with the task.

---

## Enumerated Types

All enumerations are stored as **strings** (`EnumType.STRING`) rather than ordinal integers, ensuring readability and safe reordering of enum constants.

### Role

Defines the authorization level for a user account.

| Value   | Description                                                                 |
|---------|-----------------------------------------------------------------------------|
| `USER`  | Standard user. Can create, read, update, and delete their own tasks.        |
| `ADMIN` | Administrator. Has elevated privileges, including access to all tasks.      |

**Column:** `users.role`
**JPA Mapping:** `@Enumerated(EnumType.STRING)`, stored in `VARCHAR(20)`.

---

### TaskStatus

Represents the lifecycle state of a task.

| Value         | Description                                    |
|---------------|------------------------------------------------|
| `TODO`        | Task has been created but work has not started. |
| `IN_PROGRESS` | Task is actively being worked on.               |
| `DONE`        | Task has been completed.                        |

**Column:** `tasks.status`
**JPA Mapping:** `@Enumerated(EnumType.STRING)`, stored in `VARCHAR(20)`.
**Default:** `TODO` (applied by the `@PrePersist` callback when no value is set).

---

### Priority

Indicates the urgency level of a task.

| Value    | Description                          |
|----------|--------------------------------------|
| `LOW`    | Low urgency; can be deferred.        |
| `MEDIUM` | Normal urgency; standard scheduling. |
| `HIGH`   | High urgency; should be addressed promptly. |

**Column:** `tasks.priority`
**JPA Mapping:** `@Enumerated(EnumType.STRING)`, stored in `VARCHAR(10)`.

---

## Relationships

| Relationship       | Type          | From Table | To Table | FK Column    | Fetch Strategy | Cascade | Description                        |
|--------------------|---------------|------------|----------|--------------|----------------|---------|------------------------------------|
| Task → Owner       | Many-to-One   | `tasks`    | `users`  | `owner_id`   | `EAGER`        | None    | Each task belongs to exactly one user. A user may own zero or more tasks. |

### Referential Integrity

- Deleting a `users` row that is referenced by one or more `tasks.owner_id` values will be **rejected** by the database (no cascade delete is configured).
- Applications must reassign or delete a user's tasks before removing the user account.

---

## Indexes and Constraints

### Automatically Generated

Hibernate and PostgreSQL automatically create the following indexes:

| Index / Constraint                  | Table   | Column(s)          | Type           | Purpose                                  |
|-------------------------------------|---------|---------------------|----------------|------------------------------------------|
| `users_pkey`                        | `users` | `id`                | Primary Key    | Unique row identification.               |
| `tasks_pkey`                        | `tasks` | `id`                | Primary Key    | Unique row identification.               |
| Unique constraint on `username`     | `users` | `username`          | Unique Index   | Prevents duplicate usernames.            |
| Unique constraint on `email`        | `users` | `email`             | Unique Index   | Prevents duplicate email addresses.      |
| FK constraint on `owner_id`         | `tasks` | `owner_id`          | Foreign Key    | Enforces referential integrity to `users`.|

### Query Access Patterns

The repository layer executes the following query patterns, which benefit from the indexes above and may warrant additional composite indexes at scale:

| Repository Method                                  | Indexed Columns Used                  | Notes                                           |
|----------------------------------------------------|---------------------------------------|-------------------------------------------------|
| `findByOwnerId(ownerId, pageable)`                 | `owner_id`                            | Primary task listing per user.                  |
| `findByOwnerIdAndStatus(ownerId, status, pageable)` | `owner_id`, `status`                 | Filter tasks by status within a user's scope.   |
| `findByOwnerIdAndPriority(ownerId, priority, ...)`  | `owner_id`, `priority`              | Filter tasks by priority within a user's scope. |
| `findByOwnerIdAndStatusAndPriority(...)`            | `owner_id`, `status`, `priority`     | Combined filter on status and priority.         |
| `findByStatus(status, pageable)`                    | `status`                             | Global filter by status (admin use).            |
| `findByPriority(priority, pageable)`                | `priority`                           | Global filter by priority (admin use).          |
| `findByStatusAndPriority(status, priority, ...)`    | `status`, `priority`                 | Global combined filter (admin use).             |
| `findByUsername(username)`                           | `username` (unique)                  | User lookup during authentication.              |
| `findByEmail(email)`                                 | `email` (unique)                    | User lookup by email.                           |
| `existsByUsername(username)`                          | `username` (unique)                  | Registration duplicate check.                   |
| `existsByEmail(email)`                               | `email` (unique)                    | Registration duplicate check.                   |

> **Performance Recommendation:** For production workloads with a large volume of tasks, consider adding a composite index on `(owner_id, status, priority)` to optimize the most common filtered queries.

---

## Schema Management

### Strategy

| Environment  | `ddl-auto` Strategy | Behavior                                                                  |
|--------------|----------------------|---------------------------------------------------------------------------|
| Production   | `update`             | Hibernate applies **additive** schema changes (new columns, tables) on startup. It never drops existing columns or tables. |
| Test         | `create-drop`        | Schema is created on context startup and dropped on shutdown, ensuring a clean state for each test run. |

### Lifecycle Callbacks

The domain entities use JPA lifecycle annotations to manage audit timestamps automatically:

| Annotation    | Entity | Behavior                                                        |
|---------------|--------|-----------------------------------------------------------------|
| `@PrePersist` | `User` | Sets `created_at` to the current timestamp.                     |
| `@PrePersist` | `Task` | Sets `created_at` and `updated_at` to the current timestamp. Defaults `status` to `TODO` if null. |
| `@PreUpdate`  | `Task` | Refreshes `updated_at` to the current timestamp.                |

---

## Environment Configuration

Database connectivity is configured via environment variables with sensible defaults for local development.

| Variable                   | Description                     | Default                                          |
|----------------------------|---------------------------------|--------------------------------------------------|
| `SPRING_DATASOURCE_URL`      | JDBC connection URL             | `jdbc:postgresql://localhost:5432/taskmanager`   |
| `SPRING_DATASOURCE_USERNAME` | Database username               | `taskuser`                                       |
| `SPRING_DATASOURCE_PASSWORD` | Database password               | `taskpassword`                                   |

### Docker Compose (Local Development)

The `docker-compose.yml` provisions a PostgreSQL 16 container with the following defaults:

```yaml
POSTGRES_DB:       taskmanager
POSTGRES_USER:     taskuser
POSTGRES_PASSWORD: taskpassword
Port:              5432
Volume:            postgres_data (persistent)
```

Start the database locally:

```bash
docker compose up -d db
```

---

## Testing Configuration

The test profile (`src/test/resources/application.yml`) substitutes an **H2 in-memory database** by default, removing the requirement for a running PostgreSQL instance during unit tests.

| Property             | Test Default                       | CI Override (via env var)                          |
|----------------------|------------------------------------|----------------------------------------------------|
| Datasource URL       | `jdbc:h2:mem:testdb`               | `SPRING_DATASOURCE_URL`                            |
| Driver               | `org.h2.Driver`                    | `SPRING_DATASOURCE_DRIVER`                         |
| Dialect              | `org.hibernate.dialect.H2Dialect`  | `SPRING_JPA_DIALECT`                               |
| DDL Strategy         | `create-drop`                      | —                                                  |

In CI pipelines, environment variables override these defaults to run tests against a real PostgreSQL instance for integration-level confidence.

---

## DDL Reference

The following SQL represents the equivalent schema as generated by Hibernate. It is provided for reference only; the application manages schema creation automatically.

```sql
-- ============================================================================
-- Users Table
-- ============================================================================
CREATE TABLE users (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(50)     NOT NULL UNIQUE,
    email       VARCHAR(100)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(20)     NOT NULL,
    created_at  TIMESTAMP
);

-- ============================================================================
-- Tasks Table
-- ============================================================================
CREATE TABLE tasks (
    id          BIGSERIAL       PRIMARY KEY,
    title       VARCHAR(200)    NOT NULL,
    description TEXT,
    status      VARCHAR(20)     NOT NULL,
    priority    VARCHAR(10)     NOT NULL,
    due_date    DATE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    owner_id    BIGINT          NOT NULL,

    CONSTRAINT fk_tasks_owner
        FOREIGN KEY (owner_id)
        REFERENCES users (id)
);

-- ============================================================================
-- Recommended Composite Index (for production workloads)
-- ============================================================================
CREATE INDEX idx_tasks_owner_status_priority
    ON tasks (owner_id, status, priority);
```

---

*This document is maintained alongside the codebase. For API-level details, see [API_REFERENCE.md](API_REFERENCE.md). For deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md).*

