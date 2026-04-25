package com.user_service.service;
import com.user_service.dto.LoginRequest;
import com.user_service.dto.LoginResponse;
import com.user_service.model.User;
import com.user_service.repository.UserRepository;
import com.user_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private OtpService otpService;
    @InjectMocks
    private AuthService authService;
    private User activeUser;
    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setEmail("test@test.com");
        activeUser.setPassword("encodedPassword");
        activeUser.setRole("EMPLOYEE");
        activeUser.setActive(true);
    }
    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), eq("encodedPassword"))).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com", "EMPLOYEE")).thenReturn("mock-token");
        when(userRepository.save(any())).thenReturn(activeUser);
        LoginResponse response = authService.login(new LoginRequest("test@test.com", java.util.Base64.getEncoder().encodeToString("password".getBytes())));
        assertEquals("Login successful", response.getMessage());
        assertEquals("mock-token", response.getToken());
        assertEquals("EMPLOYEE", response.getRole());
    }
    @Test
    void login_shouldThrow401_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("unknown@test.com", "password")));
        assertEquals(401, ex.getStatusCode().value());
    }
    @Test
    void login_shouldThrow401_whenAccountDisabled() {
        activeUser.setActive(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(activeUser));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("test@test.com", "password")));
        assertEquals(401, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("disabled"));
    }
    @Test
    void login_shouldThrow401_whenPasswordWrong() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), eq("encodedPassword"))).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("test@test.com", java.util.Base64.getEncoder().encodeToString("wrongPassword".getBytes()))));
        assertEquals(401, ex.getStatusCode().value());
    }
    @Test
    void forgotPassword_shouldSendOtp_whenEmailExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(activeUser));
        doNothing().when(otpService).generateAndSendOtp("test@test.com");
        Map<String, String> result = authService.forgotPassword("test@test.com");
        assertEquals("OTP sent to test@test.com", result.get("message"));
        verify(otpService).generateAndSendOtp("test@test.com");
    }
    @Test
    void forgotPassword_shouldThrow404_whenEmailNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.forgotPassword("unknown@test.com"));
        assertEquals(404, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldSucceed_whenOtpValid() {
        when(otpService.verifyOtp("test@test.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("encodedNew");
        when(userRepository.updatePassword("test@test.com", "encodedNew")).thenReturn(1);
        doNothing().when(otpService).invalidateOtp("test@test.com");
        Map<String, String> result = authService.resetPassword("test@test.com", "123456", "NewPass@123");
        assertEquals("Password reset successfully", result.get("message"));
        verify(otpService).invalidateOtp("test@test.com");
    }
    @Test
    void resetPassword_shouldThrow400_whenOtpInvalid() {
        when(otpService.verifyOtp("test@test.com", "000000")).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword("test@test.com", "000000", "NewPass@123"));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldThrow404_whenUserNotFoundInDb() {
        when(otpService.verifyOtp("test@test.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("encodedNew");
        when(userRepository.updatePassword("test@test.com", "encodedNew")).thenReturn(0);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword("test@test.com", "123456", "NewPass@123"));
        assertEquals(404, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldThrow400_whenFieldsBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword("", "123456", "NewPass@123"));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldThrow400_whenOtpNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword("test@test.com", null, "NewPass@123"));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldThrow400_whenPasswordNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword("test@test.com", "123456", null));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldThrow400_whenEmailNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword(null, "123456", "NewPass@123"));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldThrow400_whenNewPasswordBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword("test@test.com", "123456", "   "));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldThrow400_whenOtpBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword("test@test.com", "   ", "NewPass@123"));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void resetPassword_shouldSucceed_withBase64Password() {
        String encoded = java.util.Base64.getEncoder().encodeToString("NewPass@123".getBytes());
        when(otpService.verifyOtp("test@test.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNew");
        when(userRepository.updatePassword(eq("test@test.com"), anyString())).thenReturn(1);
        doNothing().when(otpService).invalidateOtp("test@test.com");
        Map<String, String> result = authService.resetPassword("test@test.com", "123456", encoded);
        assertEquals("Password reset successfully", result.get("message"));
    }
    @Test
    void login_shouldSucceed_withPlainPassword_whenBase64DecodeFails() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), eq("encodedPassword"))).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com", "EMPLOYEE")).thenReturn("mock-token");
        when(userRepository.save(any())).thenReturn(activeUser);
        LoginResponse response = authService.login(new LoginRequest("test@test.com", "plainpassword"));
        assertNotNull(response.getToken());
    }
}