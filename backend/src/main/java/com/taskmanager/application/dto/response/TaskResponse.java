package com.taskmanager.application.dto.response;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long ownerId;
    private String ownerUsername;

    public TaskResponse() {}
    public TaskResponse(Long id, String title, String description, TaskStatus status, Priority priority,
                        LocalDate dueDate, LocalDateTime createdAt, LocalDateTime updatedAt, Long ownerId, String ownerUsername) {
        this.id = id; this.title = title; this.description = description; this.status = status;
        this.priority = priority; this.dueDate = dueDate; this.createdAt = createdAt;
        this.updatedAt = updatedAt; this.ownerId = ownerId; this.ownerUsername = ownerUsername;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public static TaskResponseBuilder builder() { return new TaskResponseBuilder(); }

    public static class TaskResponseBuilder {
        private Long id;
        private String title;
        private String description;
        private TaskStatus status;
        private Priority priority;
        private LocalDate dueDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long ownerId;
        private String ownerUsername;

        public TaskResponseBuilder id(Long id) { this.id = id; return this; }
        public TaskResponseBuilder title(String title) { this.title = title; return this; }
        public TaskResponseBuilder description(String description) { this.description = description; return this; }
        public TaskResponseBuilder status(TaskStatus status) { this.status = status; return this; }
        public TaskResponseBuilder priority(Priority priority) { this.priority = priority; return this; }
        public TaskResponseBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public TaskResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TaskResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public TaskResponseBuilder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
        public TaskResponseBuilder ownerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; return this; }

        public TaskResponse build() {
            return new TaskResponse(id, title, description, status, priority, dueDate, createdAt, updatedAt, ownerId, ownerUsername);
        }
    }
}
