package com.user_service.config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SecurityConfigTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Test
    void passwordEncoder_shouldBeConfigured() {
        assertNotNull(passwordEncoder);
    }
    @Test
    void passwordEncoder_shouldEncodeAndMatch() {
        String raw = "TestPass@123";
        String encoded = passwordEncoder.encode(raw);
        assertNotNull(encoded);
        assertNotEquals(raw, encoded);
        assertTrue(passwordEncoder.matches(raw, encoded));
    }
    @Test
    void passwordEncoder_shouldNotMatchWrongPassword() {
        String encoded = passwordEncoder.encode("correct");
        assertFalse(passwordEncoder.matches("wrong", encoded));
    }
}