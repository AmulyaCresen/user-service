package com.user_service.controller;
import com.user_service.dto.LoginRequest;
import com.user_service.dto.LoginResponse;
import com.user_service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.forgotPassword(body.get("email")));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.resetPassword(
                body.get("email"), body.get("otp"), body.get("newPassword")));
    }
}