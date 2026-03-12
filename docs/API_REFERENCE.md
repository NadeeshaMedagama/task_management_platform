# API Reference

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080/api`  
**Content-Type:** `application/json`  
**Interactive Docs:** [Swagger UI](http://localhost:8080/api/swagger-ui.html) · [OpenAPI JSON](http://localhost:8080/api/v3/api-docs)

> For extended documentation with full request/response examples for every endpoint, see [API_DOCUMENTATION.md](../API_DOCUMENTATION.md).

---

## Endpoint Summary

| Method   | Endpoint                  | Auth       | Description              |
|----------|---------------------------|------------|--------------------------|
| `POST`   | `/api/auth/register`      | Public     | Register a new user      |
| `POST`   | `/api/auth/login`         | Public     | Authenticate a user      |
| `GET`    | `/api/tasks`              | Bearer JWT | List tasks (paginated)   |
| `POST`   | `/api/tasks`              | Bearer JWT | Create a task            |
| `GET`    | `/api/tasks/{id}`         | Bearer JWT | Get a task by ID         |
| `PUT`    | `/api/tasks/{id}`         | Bearer JWT | Update a task (full)     |
| `PATCH`  | `/api/tasks/{id}/status`  | Bearer JWT | Update task status only  |
| `DELETE` | `/api/tasks/{id}`         | Bearer JWT | Delete a task            |
| `GET`    | `/api/users`              | ADMIN only | List all users           |

---

## Authentication

All protected endpoints require a JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Tokens are obtained through the **Register** or **Login** endpoints and are valid for **24 hours** by default.

### POST `/api/auth/register`

Creates a new `USER` account and returns a JWT token.

**Request Body**

```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret123"
}
```

| Field      | Type   | Required | Constraints               |
|------------|--------|----------|---------------------------|
| `username` | string | ✅       | 3–50 characters, unique   |
| `email`    | string | ✅       | Valid email format, unique |
| `password` | string | ✅       | 6–100 characters          |

**Response** — `201 Created`

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "USER"
}
```

**Errors:** `400` Validation failed · `409` Username or email already exists

---

### POST `/api/auth/login`

Authenticates an existing user and returns a JWT token.

**Request Body**

```json
{
  "username": "johndoe",
  "password": "secret123"
}
```

| Field      | Type   | Required | Constraints |
|------------|--------|----------|-------------|
| `username` | string | ✅       | Not blank   |
| `password` | string | ✅       | Not blank   |

**Response** — `200 OK` (same schema as register)

**Errors:** `400` Validation failed · `401` Invalid credentials

---

## Tasks

All task endpoints require a valid JWT token. **`USER`** role can only access own tasks; **`ADMIN`** role can access all tasks.

### GET `/api/tasks`

Returns a paginated, filterable list of tasks.

**Query Parameters**

| Parameter   | Type    | Default     | Options                                    |
|-------------|---------|-------------|--------------------------------------------|
| `status`    | string  | —           | `TODO`, `IN_PROGRESS`, `DONE`              |
| `priority`  | string  | —           | `LOW`, `MEDIUM`, `HIGH`                    |
| `page`      | integer | `0`         | Zero-based page index                      |
| `size`      | integer | `10`        | Items per page                             |
| `sortBy`    | string  | `createdAt` | `createdAt`, `dueDate`, `priority`         |
| `direction` | string  | `desc`      | `asc`, `desc`                              |

**Response** — `200 OK` — Spring Data `Page<TaskResponse>`

```json
{
  "content": [ { /* TaskResponse */ } ],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false,
  "empty": false
}
```

---

### GET `/api/tasks/{id}`

Returns a single task by ID.

**Response** — `200 OK` — `TaskResponse`

**Errors:** `403` Not the task owner · `404` Task not found

---

### POST `/api/tasks`

Creates a new task assigned to the authenticated user.

**Request Body**

```json
{
  "title": "Fix login bug",
  "description": "The login form fails on mobile Safari",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-04-01"
}
```

| Field         | Type   | Required | Constraints                                     |
|---------------|--------|----------|-------------------------------------------------|
| `title`       | string | ✅       | Not blank; max 200 characters                   |
| `description` | string | ❌       | Max 2000 characters                             |
| `status`      | string | ✅       | `TODO`, `IN_PROGRESS`, or `DONE`                |
| `priority`    | string | ✅       | `LOW`, `MEDIUM`, or `HIGH`                      |
| `dueDate`     | string | ❌       | ISO-8601 date (`YYYY-MM-DD`); today or future   |

**Response** — `201 Created` — `TaskResponse`

**Errors:** `400` Validation failed

---

### PUT `/api/tasks/{id}`

Fully updates an existing task. All required fields must be provided.

**Request Body** — Same as [Create Task](#post-apitasks)

**Response** — `200 OK` — `TaskResponse`

**Errors:** `400` Validation failed · `403` Not the task owner · `404` Task not found

---

### PATCH `/api/tasks/{id}/status`

Updates only the status of a task. Used for the quick status toggle feature.

**Request Body**

```json
{
  "status": "DONE"
}
```

| Field    | Type   | Required | Options                          |
|----------|--------|----------|----------------------------------|
| `status` | string | ✅       | `TODO`, `IN_PROGRESS`, `DONE`    |

**Response** — `200 OK` — `TaskResponse`

**Errors:** `400` Validation failed · `403` Not the task owner · `404` Task not found

---

### DELETE `/api/tasks/{id}`

Permanently deletes a task.

**Response** — `204 No Content`

**Errors:** `403` Not the task owner · `404` Task not found

---

## Users

### GET `/api/users`

Returns all registered users. **Restricted to `ADMIN` role.**

**Response** — `200 OK` — `UserResponse[]`

```json
[
  {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER",
    "createdAt": "2026-03-01T08:00:00"
  }
]
```

**Errors:** `403` Caller is not an admin

---

## Response Schemas

### `TaskResponse`

| Field           | Type   | Description                                    |
|-----------------|--------|------------------------------------------------|
| `id`            | long   | Task identifier                                |
| `title`         | string | Task title                                     |
| `description`   | string | Task description (omitted if null)             |
| `status`        | string | `TODO`, `IN_PROGRESS`, or `DONE`               |
| `priority`      | string | `LOW`, `MEDIUM`, or `HIGH`                     |
| `dueDate`       | string | `YYYY-MM-DD` (omitted if null)                 |
| `createdAt`     | string | ISO-8601 timestamp                             |
| `updatedAt`     | string | ISO-8601 timestamp                             |
| `ownerId`       | long   | Owner's user ID                                |
| `ownerUsername`  | string | Owner's username                               |

### `AuthResponse`

| Field       | Type   | Description                       |
|-------------|--------|-----------------------------------|
| `token`     | string | JWT access token                  |
| `tokenType` | string | Always `"Bearer"`                 |
| `userId`    | long   | User identifier                   |
| `username`  | string | Username                          |
| `email`     | string | Email address                     |
| `role`      | string | `USER` or `ADMIN`                 |

### `UserResponse`

| Field       | Type   | Description                       |
|-------------|--------|-----------------------------------|
| `id`        | long   | User identifier                   |
| `username`  | string | Username                          |
| `email`     | string | Email address                     |
| `role`      | string | `USER` or `ADMIN`                 |
| `createdAt` | string | ISO-8601 timestamp                |

---

## Error Handling

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-03-11T09:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tasks",
  "validationErrors": [
    "Title is required",
    "Priority is required"
  ]
}
```

| Field              | Type     | Presence    | Description                                   |
|--------------------|----------|-------------|-----------------------------------------------|
| `timestamp`        | string   | Always      | When the error occurred (ISO-8601)            |
| `status`           | integer  | Always      | HTTP status code                              |
| `error`            | string   | Always      | HTTP reason phrase                            |
| `message`          | string   | Always      | Human-readable description                    |
| `path`             | string   | Always      | Request path that caused the error            |
| `validationErrors` | string[] | On `400`    | Field-level validation failure messages       |

### HTTP Status Codes

| Code  | Meaning               | When Returned                                        |
|-------|-----------------------|------------------------------------------------------|
| `200` | OK                    | Successful retrieval or update                       |
| `201` | Created               | Successful resource creation                         |
| `204` | No Content            | Successful deletion                                  |
| `400` | Bad Request           | Validation failure or malformed request              |
| `401` | Unauthorized          | Missing/invalid JWT or bad credentials               |
| `403` | Forbidden             | Insufficient permissions or ownership violation      |
| `404` | Not Found             | Resource does not exist                              |
| `409` | Conflict              | Duplicate username or email                          |
| `500` | Internal Server Error | Unexpected server error                              |

---

## Enumerations

### Task Status

| Value          | Description            |
|----------------|------------------------|
| `TODO`         | Not started            |
| `IN_PROGRESS`  | Currently in progress  |
| `DONE`         | Completed              |

### Priority

| Value    | Description     |
|----------|-----------------|
| `LOW`    | Low priority    |
| `MEDIUM` | Medium priority |
| `HIGH`   | High priority   |

### Role

| Value   | Description                                |
|---------|--------------------------------------------|
| `USER`  | Standard user — manages own tasks          |
| `ADMIN` | Administrator — manages all tasks & users  |

---

## Quick cURL Examples

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"secret123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"secret123"}'

# Create task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"title":"New task","status":"TODO","priority":"MEDIUM"}'

# List tasks (filtered + paginated)
curl "http://localhost:8080/api/tasks?status=TODO&priority=HIGH&page=0&size=5" \
  -H "Authorization: Bearer <token>"

# Update task status
curl -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"status":"DONE"}'

# Delete task
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer <token>"

# List users (admin only)
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>"
```

---

## Related Documentation

| Document                                      | Description                                    |
|-----------------------------------------------|------------------------------------------------|
| [Project Overview](./PROJECT_OVERVIEW.md)     | Architecture, tech stack, and features         |
| [Setup Guide](./SETUP_GUIDE.md)              | Installation and local development             |
| [Deployment & CI/CD](./DEPLOYMENT.md)         | Pipelines, Docker, and production deployment   |
| [Detailed API Docs](../API_DOCUMENTATION.md)  | Extended documentation with full examples      |

