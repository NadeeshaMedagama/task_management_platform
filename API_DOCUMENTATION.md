# Task Management Platform — API Documentation

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080/api`  
**Protocol:** HTTP / HTTPS  
**Content-Type:** `application/json`  
**Interactive Docs:** [Swagger UI](http://localhost:8080/api/swagger-ui.html) · [OpenAPI Spec](http://localhost:8080/api/v3/api-docs)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Authentication](#2-authentication)
   - 2.1 [Register](#21-register)
   - 2.2 [Login](#22-login)
3. [Tasks](#3-tasks)
   - 3.1 [List Tasks (Paginated)](#31-list-tasks-paginated)
   - 3.2 [Get Task by ID](#32-get-task-by-id)
   - 3.3 [Create Task](#33-create-task)
   - 3.4 [Update Task](#34-update-task)
   - 3.5 [Update Task Status](#35-update-task-status)
   - 3.6 [Delete Task](#36-delete-task)
4. [Users](#4-users)
   - 4.1 [List All Users](#41-list-all-users)
5. [Data Models](#5-data-models)
   - 5.1 [Enumerations](#51-enumerations)
   - 5.2 [Request Schemas](#52-request-schemas)
   - 5.3 [Response Schemas](#53-response-schemas)
6. [Error Handling](#6-error-handling)
   - 6.1 [Error Response Schema](#61-error-response-schema)
   - 6.2 [HTTP Status Codes](#62-http-status-codes)
   - 6.3 [Validation Errors](#63-validation-errors)
7. [Pagination](#7-pagination)
8. [Rate Limiting & Security Notes](#8-rate-limiting--security-notes)

---

## 1. Overview

The Task Management API is a RESTful service built with **Spring Boot 3.2** and **Java 21**. It provides endpoints for user authentication, task management (CRUD), and user administration. All protected endpoints require a valid **JSON Web Token (JWT)** issued during login or registration.

### Key Characteristics

| Attribute              | Details                                       |
|------------------------|-----------------------------------------------|
| **Architecture**       | Layered (Controller → Service → Repository)   |
| **Authentication**     | JWT Bearer Token (HMAC-SHA256)                |
| **Authorization**      | Role-Based Access Control (`USER`, `ADMIN`)   |
| **Database**           | PostgreSQL 16                                 |
| **Serialization**      | JSON (Jackson) — timestamps in ISO-8601       |
| **Null Policy**        | `null` fields are omitted from responses      |
| **Validation**         | Jakarta Bean Validation with detailed errors  |
| **API Documentation**  | SpringDoc OpenAPI 3.0 / Swagger UI            |

### Authorization Model

| Role      | Permissions                                                         |
|-----------|---------------------------------------------------------------------|
| `USER`    | Full CRUD on **own** tasks only                                     |
| `ADMIN`   | Full CRUD on **all** tasks; access to user management endpoints     |

---

## 2. Authentication

Authentication endpoints are **publicly accessible** and do not require a JWT token. A successful response returns a JWT that must be included in the `Authorization` header for all subsequent requests to protected endpoints.

**Header format for protected endpoints:**
```
Authorization: Bearer <token>
```

---

### 2.1 Register

Creates a new user account with the `USER` role and returns a JWT token.

**Endpoint**

```
POST /api/auth/register
```

**Request Body** — [`RegisterRequest`](#registerrequest)

| Field      | Type   | Required | Constraints                          |
|------------|--------|----------|--------------------------------------|
| `username` | string | ✅       | 3–50 characters                      |
| `email`    | string | ✅       | Valid email format                   |
| `password` | string | ✅       | 6–100 characters                     |

**Example Request**

```http
POST /api/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "secureP@ss123"
}
```

**Success Response** — `201 Created`

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzEwMTU...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "role": "USER"
}
```

**Error Responses**

| Status | Condition                             | Example Message                                    |
|--------|---------------------------------------|----------------------------------------------------|
| `400`  | Validation failure                    | `"Validation failed"` (with `validationErrors`)    |
| `409`  | Username or email already registered  | `"Username 'johndoe' is already taken."`           |

---

### 2.2 Login

Authenticates an existing user and returns a JWT token.

**Endpoint**

```
POST /api/auth/login
```

**Request Body** — [`LoginRequest`](#loginrequest)

| Field      | Type   | Required | Constraints   |
|------------|--------|----------|---------------|
| `username` | string | ✅       | Not blank     |
| `password` | string | ✅       | Not blank     |

**Example Request**

```http
POST /api/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "username": "johndoe",
  "password": "secureP@ss123"
}
```

**Success Response** — `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzEwMTU...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "role": "USER"
}
```

**Error Responses**

| Status | Condition               | Example Message                       |
|--------|-------------------------|---------------------------------------|
| `400`  | Validation failure      | `"Validation failed"`                 |
| `401`  | Invalid credentials     | `"Invalid username or password."`     |

---

## 3. Tasks

All task endpoints require a valid JWT token in the `Authorization` header.

- **`USER`** role: can only access and modify tasks they own.
- **`ADMIN`** role: can access and modify all tasks.

---

### 3.1 List Tasks (Paginated)

Returns a paginated list of tasks with optional filtering and sorting. Users see their own tasks; admins see all tasks.

**Endpoint**

```
GET /api/tasks
```

**Query Parameters**

| Parameter   | Type    | Required | Default     | Allowed Values                             |
|-------------|---------|----------|-------------|--------------------------------------------|
| `status`    | string  | ❌       | _(none)_    | `TODO`, `IN_PROGRESS`, `DONE`              |
| `priority`  | string  | ❌       | _(none)_    | `LOW`, `MEDIUM`, `HIGH`                    |
| `page`      | integer | ❌       | `0`         | 0-based page index                         |
| `size`      | integer | ❌       | `10`        | Number of items per page                   |
| `sortBy`    | string  | ❌       | `createdAt` | `createdAt`, `dueDate`, `priority`         |
| `direction` | string  | ❌       | `desc`      | `asc`, `desc`                              |

**Example Request**

```http
GET /api/tasks?status=TODO&priority=HIGH&page=0&size=5&sortBy=dueDate&direction=asc HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Success Response** — `200 OK`

```json
{
  "content": [
    {
      "id": 12,
      "title": "Fix login bug",
      "description": "The login form fails on mobile Safari",
      "status": "TODO",
      "priority": "HIGH",
      "dueDate": "2026-04-01",
      "createdAt": "2026-03-10T14:30:00",
      "updatedAt": "2026-03-10T14:30:00",
      "ownerId": 1,
      "ownerUsername": "johndoe"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "size": 5,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

**Error Responses**

| Status | Condition                      |
|--------|--------------------------------|
| `401`  | Missing or invalid JWT token   |

---

### 3.2 Get Task by ID

Retrieves a single task by its unique identifier. Users can only retrieve their own tasks; admins can retrieve any task.

**Endpoint**

```
GET /api/tasks/{id}
```

**Path Parameters**

| Parameter | Type | Required | Description        |
|-----------|------|----------|--------------------|
| `id`      | long | ✅       | The task identifier |

**Example Request**

```http
GET /api/tasks/12 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Success Response** — `200 OK`

```json
{
  "id": 12,
  "title": "Fix login bug",
  "description": "The login form fails on mobile Safari",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-04-01",
  "createdAt": "2026-03-10T14:30:00",
  "updatedAt": "2026-03-10T14:30:00",
  "ownerId": 1,
  "ownerUsername": "johndoe"
}
```

**Error Responses**

| Status | Condition                             | Example Message                          |
|--------|---------------------------------------|------------------------------------------|
| `401`  | Missing or invalid JWT token          | `"Authentication required..."`           |
| `403`  | User does not own the task            | `"You do not have access to this task."` |
| `404`  | Task not found                        | `"Task not found with id: 12"`           |

---

### 3.3 Create Task

Creates a new task assigned to the authenticated user.

**Endpoint**

```
POST /api/tasks
```

**Request Body** — [`TaskRequest`](#taskrequest)

| Field         | Type   | Required | Constraints                                    |
|---------------|--------|----------|------------------------------------------------|
| `title`       | string | ✅       | Not blank; max 200 characters                  |
| `description` | string | ❌       | Max 2000 characters                            |
| `status`      | string | ✅       | `TODO`, `IN_PROGRESS`, or `DONE`               |
| `priority`    | string | ✅       | `LOW`, `MEDIUM`, or `HIGH`                     |
| `dueDate`     | string | ❌       | ISO-8601 date (`YYYY-MM-DD`); today or future  |

**Example Request**

```http
POST /api/tasks HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "title": "Implement user dashboard",
  "description": "Create a dashboard page showing task statistics",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-04-15"
}
```

**Success Response** — `201 Created`

```json
{
  "id": 13,
  "title": "Implement user dashboard",
  "description": "Create a dashboard page showing task statistics",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-04-15",
  "createdAt": "2026-03-11T09:15:00",
  "updatedAt": "2026-03-11T09:15:00",
  "ownerId": 1,
  "ownerUsername": "johndoe"
}
```

**Error Responses**

| Status | Condition                    | Example Message                                |
|--------|------------------------------|------------------------------------------------|
| `400`  | Validation failure           | `"Validation failed"` (with `validationErrors`) |
| `401`  | Missing or invalid JWT token | `"Authentication required..."`                 |

---

### 3.4 Update Task

Performs a full update of an existing task. Users can only update their own tasks; admins can update any task.

**Endpoint**

```
PUT /api/tasks/{id}
```

**Path Parameters**

| Parameter | Type | Required | Description        |
|-----------|------|----------|--------------------|
| `id`      | long | ✅       | The task identifier |

**Request Body** — [`TaskRequest`](#taskrequest)

| Field         | Type   | Required | Constraints                                    |
|---------------|--------|----------|------------------------------------------------|
| `title`       | string | ✅       | Not blank; max 200 characters                  |
| `description` | string | ❌       | Max 2000 characters                            |
| `status`      | string | ✅       | `TODO`, `IN_PROGRESS`, or `DONE`               |
| `priority`    | string | ✅       | `LOW`, `MEDIUM`, or `HIGH`                     |
| `dueDate`     | string | ❌       | ISO-8601 date (`YYYY-MM-DD`); today or future  |

**Example Request**

```http
PUT /api/tasks/13 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "title": "Implement user dashboard",
  "description": "Dashboard with charts and task statistics per user",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "dueDate": "2026-04-20"
}
```

**Success Response** — `200 OK`

```json
{
  "id": 13,
  "title": "Implement user dashboard",
  "description": "Dashboard with charts and task statistics per user",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "dueDate": "2026-04-20",
  "createdAt": "2026-03-11T09:15:00",
  "updatedAt": "2026-03-11T10:45:00",
  "ownerId": 1,
  "ownerUsername": "johndoe"
}
```

**Error Responses**

| Status | Condition                             | Example Message                          |
|--------|---------------------------------------|------------------------------------------|
| `400`  | Validation failure                    | `"Validation failed"`                    |
| `401`  | Missing or invalid JWT token          | `"Authentication required..."`           |
| `403`  | User does not own the task            | `"You do not have access to this task."` |
| `404`  | Task not found                        | `"Task not found with id: 13"`           |

---

### 3.5 Update Task Status

Partially updates only the status of an existing task. This is the endpoint used for the quick status toggle feature (e.g., TODO → IN_PROGRESS → DONE).

**Endpoint**

```
PATCH /api/tasks/{id}/status
```

**Path Parameters**

| Parameter | Type | Required | Description        |
|-----------|------|----------|--------------------|
| `id`      | long | ✅       | The task identifier |

**Request Body** — [`UpdateStatusRequest`](#updatestatusrequest)

| Field    | Type   | Required | Allowed Values                   |
|----------|--------|----------|----------------------------------|
| `status` | string | ✅       | `TODO`, `IN_PROGRESS`, `DONE`    |

**Example Request**

```http
PATCH /api/tasks/13/status HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "status": "DONE"
}
```

**Success Response** — `200 OK`

```json
{
  "id": 13,
  "title": "Implement user dashboard",
  "description": "Dashboard with charts and task statistics per user",
  "status": "DONE",
  "priority": "HIGH",
  "dueDate": "2026-04-20",
  "createdAt": "2026-03-11T09:15:00",
  "updatedAt": "2026-03-11T16:00:00",
  "ownerId": 1,
  "ownerUsername": "johndoe"
}
```

**Error Responses**

| Status | Condition                             | Example Message                          |
|--------|---------------------------------------|------------------------------------------|
| `400`  | Validation failure                    | `"Validation failed"`                    |
| `401`  | Missing or invalid JWT token          | `"Authentication required..."`           |
| `403`  | User does not own the task            | `"You do not have access to this task."` |
| `404`  | Task not found                        | `"Task not found with id: 13"`           |

---

### 3.6 Delete Task

Permanently deletes a task. Users can only delete their own tasks; admins can delete any task.

**Endpoint**

```
DELETE /api/tasks/{id}
```

**Path Parameters**

| Parameter | Type | Required | Description        |
|-----------|------|----------|--------------------|
| `id`      | long | ✅       | The task identifier |

**Example Request**

```http
DELETE /api/tasks/13 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Success Response** — `204 No Content`

_(No response body)_

**Error Responses**

| Status | Condition                             | Example Message                          |
|--------|---------------------------------------|------------------------------------------|
| `401`  | Missing or invalid JWT token          | `"Authentication required..."`           |
| `403`  | User does not own the task            | `"You do not have access to this task."` |
| `404`  | Task not found                        | `"Task not found with id: 13"`           |

---

## 4. Users

User management endpoints are restricted to the **`ADMIN`** role.

---

### 4.1 List All Users

Returns a list of all registered users in the system.

**Endpoint**

```
GET /api/users
```

**Authorization:** `ADMIN` role required.

**Example Request**

```http
GET /api/users HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Success Response** — `200 OK`

```json
[
  {
    "id": 1,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "role": "USER",
    "createdAt": "2026-03-01T08:00:00"
  },
  {
    "id": 2,
    "username": "admin",
    "email": "admin@example.com",
    "role": "ADMIN",
    "createdAt": "2026-02-15T10:30:00"
  }
]
```

**Error Responses**

| Status | Condition                             | Example Message                                       |
|--------|---------------------------------------|-------------------------------------------------------|
| `401`  | Missing or invalid JWT token          | `"Authentication required..."`                        |
| `403`  | Caller does not have `ADMIN` role     | `"You do not have permission to perform this action."` |

---

## 5. Data Models

### 5.1 Enumerations

#### `Role`

| Value   | Description                          |
|---------|--------------------------------------|
| `USER`  | Standard user — manages own tasks    |
| `ADMIN` | Administrator — manages all tasks and users |

#### `TaskStatus`

| Value          | Description                          |
|----------------|--------------------------------------|
| `TODO`         | Task has not been started            |
| `IN_PROGRESS`  | Task is currently being worked on    |
| `DONE`         | Task is completed                    |

#### `Priority`

| Value    | Description          |
|----------|----------------------|
| `LOW`    | Low priority         |
| `MEDIUM` | Medium priority      |
| `HIGH`   | High priority        |

---

### 5.2 Request Schemas

#### `RegisterRequest`

Used for user registration.

| Field      | Type   | Required | Constraints                                   |
|------------|--------|----------|-----------------------------------------------|
| `username` | string | ✅       | Not blank; 3–50 characters                    |
| `email`    | string | ✅       | Not blank; valid email format                 |
| `password` | string | ✅       | Not blank; 6–100 characters                   |

```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "secureP@ss123"
}
```

---

#### `LoginRequest`

Used for user authentication.

| Field      | Type   | Required | Constraints |
|------------|--------|----------|-------------|
| `username` | string | ✅       | Not blank   |
| `password` | string | ✅       | Not blank   |

```json
{
  "username": "johndoe",
  "password": "secureP@ss123"
}
```

---

#### `TaskRequest`

Used for creating and fully updating a task.

| Field         | Type   | Required | Constraints                                              |
|---------------|--------|----------|----------------------------------------------------------|
| `title`       | string | ✅       | Not blank; max 200 characters                            |
| `description` | string | ❌       | Max 2000 characters; `null` permitted                    |
| `status`      | string | ✅       | One of: `TODO`, `IN_PROGRESS`, `DONE`                    |
| `priority`    | string | ✅       | One of: `LOW`, `MEDIUM`, `HIGH`                          |
| `dueDate`     | string | ❌       | ISO-8601 date (`YYYY-MM-DD`); must be today or in the future |

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

#### `UpdateStatusRequest`

Used for partially updating only the task status.

| Field    | Type   | Required | Constraints                           |
|----------|--------|----------|---------------------------------------|
| `status` | string | ✅       | One of: `TODO`, `IN_PROGRESS`, `DONE` |

```json
{
  "status": "IN_PROGRESS"
}
```

---

### 5.3 Response Schemas

#### `AuthResponse`

Returned by both registration and login endpoints.

| Field       | Type   | Description                              |
|-------------|--------|------------------------------------------|
| `token`     | string | JWT access token                         |
| `tokenType` | string | Token type — always `"Bearer"`           |
| `userId`    | long   | Unique identifier of the user            |
| `username`  | string | Username of the authenticated user       |
| `email`     | string | Email address of the authenticated user  |
| `role`      | string | Role assigned to the user (`USER` / `ADMIN`) |

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "role": "USER"
}
```

---

#### `TaskResponse`

Returned by all task retrieval, creation, and update endpoints.

| Field           | Type     | Description                                    |
|-----------------|----------|------------------------------------------------|
| `id`            | long     | Unique identifier of the task                  |
| `title`         | string   | Task title                                     |
| `description`   | string   | Task description (omitted if `null`)           |
| `status`        | string   | Current status (`TODO`, `IN_PROGRESS`, `DONE`) |
| `priority`      | string   | Priority level (`LOW`, `MEDIUM`, `HIGH`)       |
| `dueDate`       | string   | Due date in `YYYY-MM-DD` format (omitted if `null`) |
| `createdAt`     | string   | Creation timestamp in ISO-8601 format          |
| `updatedAt`     | string   | Last update timestamp in ISO-8601 format       |
| `ownerId`       | long     | ID of the task owner                           |
| `ownerUsername`  | string   | Username of the task owner                     |

```json
{
  "id": 12,
  "title": "Fix login bug",
  "description": "The login form fails on mobile Safari",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-04-01",
  "createdAt": "2026-03-10T14:30:00",
  "updatedAt": "2026-03-10T14:30:00",
  "ownerId": 1,
  "ownerUsername": "johndoe"
}
```

---

#### `UserResponse`

Returned by the user listing endpoint.

| Field       | Type   | Description                              |
|-------------|--------|------------------------------------------|
| `id`        | long   | Unique identifier of the user            |
| `username`  | string | Username                                 |
| `email`     | string | Email address                            |
| `role`      | string | Role (`USER` / `ADMIN`)                  |
| `createdAt` | string | Account creation timestamp (ISO-8601)    |

```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "role": "USER",
  "createdAt": "2026-03-01T08:00:00"
}
```

---

#### `PagedResponse<TaskResponse>`

Wrapper returned by paginated endpoints (Spring Data `Page` structure).

| Field               | Type    | Description                                    |
|---------------------|---------|------------------------------------------------|
| `content`           | array   | Array of [`TaskResponse`](#taskresponse) items |
| `pageable.pageNumber` | integer | Current page index (0-based)                 |
| `pageable.pageSize`   | integer | Requested page size                          |
| `pageable.sort`       | object  | Sort metadata                                |
| `totalElements`     | integer | Total number of records matching the query     |
| `totalPages`        | integer | Total number of pages                          |
| `size`              | integer | Page size                                      |
| `number`            | integer | Current page number (0-based)                  |
| `first`             | boolean | `true` if this is the first page               |
| `last`              | boolean | `true` if this is the last page                |
| `numberOfElements`  | integer | Number of elements on the current page         |
| `empty`             | boolean | `true` if the page has no content              |

---

## 6. Error Handling

The API uses a global exception handler that returns consistent, structured JSON error responses for all failure scenarios.

### 6.1 Error Response Schema

#### `ApiError`

| Field              | Type     | Presence    | Description                                          |
|--------------------|----------|-------------|------------------------------------------------------|
| `timestamp`        | string   | Always      | ISO-8601 timestamp of when the error occurred        |
| `status`           | integer  | Always      | HTTP status code                                     |
| `error`            | string   | Always      | HTTP status reason phrase (e.g., `"Bad Request"`)    |
| `message`          | string   | Always      | Human-readable error description                     |
| `path`             | string   | Always      | Request URI that caused the error                    |
| `validationErrors` | string[] | Conditional | List of field-level validation messages (on `400` only) |

**Example — Validation Error (400)**

```json
{
  "timestamp": "2026-03-11T09:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tasks",
  "validationErrors": [
    "Title is required",
    "Priority is required",
    "Due date must be today or in the future"
  ]
}
```

**Example — Not Found (404)**

```json
{
  "timestamp": "2026-03-11T09:05:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 999",
  "path": "/api/tasks/999"
}
```

**Example — Unauthorized (401)**

```json
{
  "timestamp": "2026-03-11T09:10:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Please provide a valid JWT token.",
  "path": "/api/tasks"
}
```

**Example — Forbidden (403)**

```json
{
  "timestamp": "2026-03-11T09:15:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have access to this task.",
  "path": "/api/tasks/5"
}
```

**Example — Conflict (409)**

```json
{
  "timestamp": "2026-03-11T09:20:00",
  "status": 409,
  "error": "Conflict",
  "message": "Username 'johndoe' is already taken.",
  "path": "/api/auth/register"
}
```

---

### 6.2 HTTP Status Codes

| Code  | Meaning                | When Returned                                           |
|-------|------------------------|---------------------------------------------------------|
| `200` | OK                     | Successful retrieval or update                          |
| `201` | Created                | Successful resource creation (register, create task)    |
| `204` | No Content             | Successful deletion                                     |
| `400` | Bad Request            | Validation failure or malformed input                   |
| `401` | Unauthorized           | Missing, expired, or invalid JWT token; bad credentials |
| `403` | Forbidden              | Insufficient role permissions or ownership violation    |
| `404` | Not Found              | Requested resource does not exist                       |
| `409` | Conflict               | Duplicate resource (username or email already exists)   |
| `500` | Internal Server Error  | Unexpected server-side error                            |

---

### 6.3 Validation Errors

When a request body fails validation, the API returns a `400 Bad Request` response with the `validationErrors` array populated. Each entry corresponds to a failed constraint on a specific field.

**Possible Validation Messages**

| Field         | Constraint             | Message                                           |
|---------------|------------------------|---------------------------------------------------|
| `username`    | `@NotBlank`            | `"Username is required"`                          |
| `username`    | `@Size(3, 50)`         | `"Username must be between 3 and 50 characters"`  |
| `email`       | `@NotBlank`            | `"Email is required"`                             |
| `email`       | `@Email`               | `"Email must be valid"`                           |
| `password`    | `@NotBlank`            | `"Password is required"`                          |
| `password`    | `@Size(6, 100)`        | `"Password must be at least 6 characters"`        |
| `title`       | `@NotBlank`            | `"Title is required"`                             |
| `title`       | `@Size(max=200)`       | `"Title must not exceed 200 characters"`          |
| `description` | `@Size(max=2000)`      | `"Description must not exceed 2000 characters"`   |
| `status`      | `@NotNull`             | `"Status is required"`                            |
| `priority`    | `@NotNull`             | `"Priority is required"`                          |
| `dueDate`     | `@FutureOrPresent`     | `"Due date must be today or in the future"`       |

---

## 7. Pagination

The `GET /api/tasks` endpoint returns results in a Spring Data **`Page`** wrapper. Pagination is controlled via query parameters.

### Request Parameters

| Parameter   | Default     | Description                                  |
|-------------|-------------|----------------------------------------------|
| `page`      | `0`         | Zero-based page index                        |
| `size`      | `10`        | Number of records per page                   |
| `sortBy`    | `createdAt` | Field to sort by (`createdAt`, `dueDate`, `priority`) |
| `direction` | `desc`      | Sort direction (`asc` or `desc`)             |

### Response Metadata

The response includes standard Spring Data pagination metadata alongside the `content` array:

| Field            | Description                                |
|------------------|--------------------------------------------|
| `totalElements`  | Total number of records matching the query |
| `totalPages`     | Total number of pages                      |
| `number`         | Current page index (0-based)               |
| `size`           | Page size                                  |
| `first`          | Whether this is the first page             |
| `last`           | Whether this is the last page              |
| `numberOfElements` | Number of elements on the current page  |
| `empty`          | Whether the current page is empty          |

### Example — Fetching Page 2 with 5 Items per Page

```http
GET /api/tasks?page=1&size=5&sortBy=priority&direction=asc HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 8. Rate Limiting & Security Notes

### JWT Token Details

| Property        | Value                                       |
|-----------------|---------------------------------------------|
| **Algorithm**   | HMAC-SHA256 (HS256)                         |
| **Token Type**  | Bearer                                      |
| **Expiration**  | 24 hours (86,400,000 ms) — configurable     |
| **Header**      | `Authorization: Bearer <token>`             |

### Security Recommendations

1. **Use HTTPS in production** — JWT tokens are transmitted in headers and must be encrypted in transit.
2. **Store tokens securely** — On the frontend, tokens are stored in `localStorage`. For higher security, consider `httpOnly` cookies.
3. **Rotate the JWT secret** — Change the `JWT_SECRET` environment variable periodically and upon any suspected compromise.
4. **Set strong passwords** — The minimum password length enforced by the API is 6 characters; consider enforcing stronger policies at the application level.
5. **Promote admin users manually** — New registrations default to the `USER` role. Admin promotion requires direct database access:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
   ```

### CORS

The backend does not enforce specific CORS restrictions by default in development. For production deployments, configure allowed origins in `SecurityConfig` or via environment variables.

---

## Appendix: Quick Reference

### Endpoint Summary

| Method   | Endpoint                  | Auth     | Role       | Description              |
|----------|---------------------------|----------|------------|--------------------------|
| `POST`   | `/api/auth/register`      | ❌ Public | —         | Register a new user      |
| `POST`   | `/api/auth/login`         | ❌ Public | —         | Authenticate a user      |
| `GET`    | `/api/tasks`              | ✅       | USER/ADMIN | List tasks (paginated)   |
| `POST`   | `/api/tasks`              | ✅       | USER/ADMIN | Create a task            |
| `GET`    | `/api/tasks/{id}`         | ✅       | USER/ADMIN | Get a task by ID         |
| `PUT`    | `/api/tasks/{id}`         | ✅       | USER/ADMIN | Update a task (full)     |
| `PATCH`  | `/api/tasks/{id}/status`  | ✅       | USER/ADMIN | Update task status only  |
| `DELETE` | `/api/tasks/{id}`         | ✅       | USER/ADMIN | Delete a task            |
| `GET`    | `/api/users`              | ✅       | ADMIN only | List all users           |

### cURL Examples

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"secret123"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"secret123"}'
```

**Create Task:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"title":"New task","status":"TODO","priority":"MEDIUM"}'
```

**List Tasks (filtered & paginated):**
```bash
curl -X GET "http://localhost:8080/api/tasks?status=TODO&priority=HIGH&page=0&size=10&sortBy=createdAt&direction=desc" \
  -H "Authorization: Bearer <token>"
```

**Update Task Status:**
```bash
curl -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"status":"DONE"}'
```

**Delete Task:**
```bash
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer <token>"
```

**List Users (admin only):**
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>"
```

---

_This documentation reflects API version **1.0.0**. For the interactive API explorer, visit the [Swagger UI](http://localhost:8080/api/swagger-ui.html) after starting the backend service._

