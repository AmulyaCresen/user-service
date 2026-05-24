package com.user_service;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserServiceApplicationTests {
	@Test
	void contextLoads() {
	}
	@Test
	void applicationInstance_shouldReturnName() {
		UserServiceApplication app = new UserServiceApplication();
		assertNotNull(app);
		assertEquals("user-service", app.getApplicationName());
	}
	@Test
	void applicationName_shouldNotBeEmpty() {
		UserServiceApplication app = new UserServiceApplication();
		assertFalse(app.getApplicationName().isEmpty());
	}
}