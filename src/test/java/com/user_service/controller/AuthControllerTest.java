package com.user_service.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_service.dto.LoginRequest;
import com.user_service.dto.LoginResponse;
import com.user_service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;
    @Test
    @WithMockUser
    void login_shouldReturn200_whenCredentialsValid() throws Exception {
        LoginResponse response = new LoginResponse("Login successful", "mock-token", "EMPLOYEE", "Test User", "test@test.com", "Male");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);
        mockMvc.perform(post("/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("test@test.com", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }
    @Test
    @WithMockUser
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        mockMvc.perform(post("/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("test@test.com", "wrong"))))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser
    void forgotPassword_shouldReturn200_whenEmailExists() throws Exception {
        when(authService.forgotPassword("test@test.com"))
                .thenReturn(Map.of("message", "OTP sent to test@test.com"));
        mockMvc.perform(post("/auth/forgot-password").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to test@test.com"));
    }
    @Test
    @WithMockUser
    void forgotPassword_shouldReturn404_whenEmailNotFound() throws Exception {
        when(authService.forgotPassword("unknown@test.com"))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "No account found"));
        mockMvc.perform(post("/auth/forgot-password").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"unknown@test.com\"}"))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser
    void resetPassword_shouldReturn200_whenOtpValid() throws Exception {
        when(authService.resetPassword("test@test.com", "123456", "NewPass@123"))
                .thenReturn(Map.of("message", "Password reset successfully"));
        String body = "{\"email\":\"test@test.com\",\"otp\":\"123456\",\"newPassword\":\"NewPass@123\"}";
        mockMvc.perform(post("/auth/reset-password").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }
    @Test
    @WithMockUser
    void resetPassword_shouldReturn400_whenOtpInvalid() throws Exception {
        when(authService.resetPassword(anyString(), anyString(), anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired OTP"));
        String body = "{\"email\":\"test@test.com\",\"otp\":\"000000\",\"newPassword\":\"NewPass@123\"}";
        mockMvc.perform(post("/auth/reset-password").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
}