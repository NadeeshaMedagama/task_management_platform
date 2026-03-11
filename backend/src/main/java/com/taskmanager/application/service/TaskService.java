package com.taskmanager.application.service;

import com.taskmanager.application.dto.request.TaskRequest;
import com.taskmanager.application.dto.request.UpdateStatusRequest;
import com.taskmanager.application.dto.response.TaskResponse;
import com.taskmanager.application.mapper.TaskMapper;
import com.taskmanager.domain.model.*;
import com.taskmanager.domain.repository.TaskRepository;
import com.taskmanager.domain.repository.UserRepository;
import com.taskmanager.web.exception.AccessDeniedException;
import com.taskmanager.web.exception.ResourceNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(TaskStatus status, Priority priority,
                                        int page, int size, String sortBy, String direction) {
        User currentUser = getCurrentUser();
        Sort sort = buildSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> tasks;
        if (currentUser.getRole() == Role.ADMIN) {
            tasks = findAllTasks(status, priority, pageable);
        } else {
            tasks = findUserTasks(currentUser.getId(), status, priority, pageable);
        }

        return tasks.map(taskMapper::toResponse);
    }

    private Page<Task> findAllTasks(TaskStatus status, Priority priority, Pageable pageable) {
        if (status != null && priority != null) {
            return taskRepository.findByStatusAndPriority(status, priority, pageable);
        } else if (status != null) {
            return taskRepository.findByStatus(status, pageable);
        } else if (priority != null) {
            return taskRepository.findByPriority(priority, pageable);
        } else {
            return taskRepository.findAll(pageable);
        }
    }

    private Page<Task> findUserTasks(Long ownerId, TaskStatus status, Priority priority, Pageable pageable) {
        if (status != null && priority != null) {
            return taskRepository.findByOwnerIdAndStatusAndPriority(ownerId, status, priority, pageable);
        } else if (status != null) {
            return taskRepository.findByOwnerIdAndStatus(ownerId, status, pageable);
        } else if (priority != null) {
            return taskRepository.findByOwnerIdAndPriority(ownerId, priority, pageable);
        } else {
            return taskRepository.findByOwnerId(ownerId, pageable);
        }
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = findTaskById(id);
        checkOwnershipOrAdmin(task);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User currentUser = getCurrentUser();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .owner(currentUser)
                .build();

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTaskById(id);
        checkOwnershipOrAdmin(task);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, UpdateStatusRequest request) {
        Task task = findTaskById(id);
        checkOwnershipOrAdmin(task);
        task.setStatus(request.getStatus());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskById(id);
        checkOwnershipOrAdmin(task);
        taskRepository.delete(task);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
    }

    private void checkOwnershipOrAdmin(Task task) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN &&
                !task.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this task.");
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Sort buildSort(String sortBy, String direction) {
        String field = switch (sortBy != null ? sortBy : "createdAt") {
            case "dueDate" -> "dueDate";
            case "priority" -> "priority";
            default -> "createdAt";
        };
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, field);
    }
}
