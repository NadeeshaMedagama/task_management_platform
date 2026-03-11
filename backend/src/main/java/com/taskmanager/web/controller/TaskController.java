package com.taskmanager.web.controller;

import com.taskmanager.application.dto.request.TaskRequest;
import com.taskmanager.application.dto.request.UpdateStatusRequest;
import com.taskmanager.application.dto.response.TaskResponse;
import com.taskmanager.application.service.TaskService;
import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Returns paginated tasks. ADMIN sees all; USER sees own tasks only.")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(taskService.getTasks(status, priority, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                    @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Used to mark a task as TODO, IN_PROGRESS or DONE")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}

