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

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole("EMPLOYEE");
        testUser.setGender("Male");
        testUser.setActive(true);
        testUser.setCreateDate(OffsetDateTime.now());

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword(Base64.getEncoder().encodeToString("password123".getBytes()));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals("jwt-token", response.getToken());
        assertEquals("EMPLOYEE", response.getRole());
        assertEquals("Test User", response.getFullName());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testLogin_WithPlainPassword() {
        loginRequest.setPassword("plainPassword");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_AccountDisabled() {
        testUser.setActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(ResponseStatusException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testForgotPassword_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(otpService).generateAndSendOtp(anyString());

        Map<String, String> response = authService.forgotPassword("test@example.com");

        assertNotNull(response);
        assertTrue(response.get("message").contains("OTP sent"));
        verify(otpService).generateAndSendOtp("test@example.com");
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authService.forgotPassword("test@example.com"));
    }

    @Test
    void testResetPassword_Success() {
        String encodedPassword = Base64.getEncoder().encodeToString("newPassword123".getBytes());
        
        when(otpService.verifyOtp("test@example.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.updatePassword(anyString(), anyString())).thenReturn(1);
        doNothing().when(otpService).invalidateOtp(anyString());

        Map<String, String> response = authService.resetPassword("test@example.com", "123456", encodedPassword);

        assertNotNull(response);
        assertEquals("Password reset successfully", response.get("message"));
        verify(userRepository).updatePassword("test@example.com", "encodedNewPassword");
        verify(otpService).invalidateOtp("test@example.com");
    }

    @Test
    void testResetPassword_WithPlainPassword() {
        when(otpService.verifyOtp("test@example.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.updatePassword(anyString(), anyString())).thenReturn(1);
        doNothing().when(otpService).invalidateOtp(anyString());

        Map<String, String> response = authService.resetPassword("test@example.com", "123456", "plainPassword");

        assertNotNull(response);
        assertEquals("Password reset successfully", response.get("message"));
    }

    @Test
    void testResetPassword_NullEmail() {
        assertThrows(ResponseStatusException.class, () -> 
            authService.resetPassword(null, "123456", "newPassword"));
    }

    @Test
    void testResetPassword_BlankEmail() {
        assertThrows(ResponseStatusException.class, () -> 
            authService.resetPassword("", "123456", "newPassword"));
    }

    @Test
    void testResetPassword_NullOtp() {
        assertThrows(ResponseStatusException.class, () -> 
            authService.resetPassword("test@example.com", null, "newPassword"));
    }

    @Test
    void testResetPassword_BlankOtp() {
        assertThrows(ResponseStatusException.class, () -> 
            authService.resetPassword("test@example.com", "", "newPassword"));
    }

    @Test
    void testResetPassword_NullPassword() {
        assertThrows(ResponseStatusException.class, () -> 
            authService.resetPassword("test@example.com", "123456", null));
    }

    @Test
    void testResetPassword_BlankPassword() {
        assertThrows(ResponseStatusException.class, () -> 
            authService.resetPassword("test@example.com", "123456", ""));
    }

    @Test
    void testResetPassword_InvalidOtp() {
        when(otpService.verifyOtp("test@example.com", "123456")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> 
            authService.resetPassword("test@example.com", "123456", "newPassword"));
    }

    @Test
    void testResetPassword_UserNotFound() {
        when(otpService.verifyOtp("test@example.com", "123456")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.updatePassword(anyString(), anyString())).thenReturn(0);

        assertThrows(ResponseStatusException.class, () -> 
            authService.resetPassword("test@example.com", "123456", "newPassword"));
    }
}
