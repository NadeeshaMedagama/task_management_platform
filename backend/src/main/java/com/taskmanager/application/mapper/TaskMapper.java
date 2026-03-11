package com.taskmanager.application.mapper;

import com.taskmanager.application.dto.response.TaskResponse;
import com.taskmanager.domain.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .ownerId(task.getOwner().getId())
                .ownerUsername(task.getOwner().getUsername())
                .build();
    }
}

