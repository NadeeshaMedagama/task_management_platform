package com.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TaskManagementApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context loads without errors
    }
}

