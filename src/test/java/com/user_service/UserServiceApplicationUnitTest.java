package com.user_service;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
class UserServiceApplicationUnitTest {
    @Test
    void applicationInstance_shouldBeCreated() {
        UserServiceApplication app = new UserServiceApplication();
        assertNotNull(app);
    }
    @Test
    void getApplicationName_shouldReturnUserService() {
        UserServiceApplication app = new UserServiceApplication();
        assertEquals("user-service", app.getApplicationName());
    }
}