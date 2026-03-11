package com.taskmanager.application.dto.request;

import com.taskmanager.domain.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;

    public UpdateStatusRequest() {}

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
