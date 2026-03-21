package com.taskmanager.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Avoid framework-specific ObjectMapper coupling in the security entry point.
        String body = String.format(
                "{\"status\":%d,\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please provide a valid JWT token.\",\"path\":\"%s\"}",
                HttpStatus.UNAUTHORIZED.value(),
                escapeJson(request.getRequestURI())
        );
        response.getWriter().write(body);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
