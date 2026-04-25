package com.user_service.util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
class JwtUtilTest {
    private JwtUtil jwtUtil;
    private static final String SECRET = "LeaveManagementSystemJWTSecretKey2026MinLength32Chars!";
    private static final long EXPIRATION = 86400000L;
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }
    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtil.generateToken("test@test.com", "EMPLOYEE");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }
    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtUtil.generateToken("test@test.com", "EMPLOYEE");
        assertTrue(jwtUtil.validateToken(token));
    }
    @Test
    void validateToken_shouldReturnFalse_forInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }
    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken("test@test.com", "ADMIN");
        assertEquals("test@test.com", jwtUtil.extractEmail(token));
    }
}