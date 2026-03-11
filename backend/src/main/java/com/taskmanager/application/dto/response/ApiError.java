package com.taskmanager.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> validationErrors;

    public ApiError() {}
    public ApiError(LocalDateTime timestamp, int status, String error, String message, String path, List<String> validationErrors) {
        this.timestamp = timestamp; this.status = status; this.error = error;
        this.message = message; this.path = path; this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public List<String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; }

    public static ApiErrorBuilder builder() { return new ApiErrorBuilder(); }

    public static class ApiErrorBuilder {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private List<String> validationErrors;

        public ApiErrorBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public ApiErrorBuilder status(int status) { this.status = status; return this; }
        public ApiErrorBuilder error(String error) { this.error = error; return this; }
        public ApiErrorBuilder message(String message) { this.message = message; return this; }
        public ApiErrorBuilder path(String path) { this.path = path; return this; }
        public ApiErrorBuilder validationErrors(List<String> validationErrors) { this.validationErrors = validationErrors; return this; }

        public ApiError build() {
            return new ApiError(timestamp, status, error, message, path, validationErrors);
        }
    }
}
