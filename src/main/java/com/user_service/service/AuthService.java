package com.user_service.service;

import com.user_service.dto.LoginRequest;
import com.user_service.dto.LoginResponse;
import com.user_service.model.User;
import com.user_service.repository.UserRepository;
import com.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    public LoginResponse login(LoginRequest request) {
        log.info("[AUTH] Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[AUTH] Login failed — user not found: {}", request.getEmail());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (!user.isActive()) {
            log.warn("[AUTH] Login failed — account disabled: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("[AUTH] Login failed — wrong password for: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        user.setLastLogin(java.time.OffsetDateTime.now());
        userRepository.save(user);
        log.info("[AUTH] ✅ Login successful: {} | role: {}", user.getEmail(), user.getRole());
        return new LoginResponse("Login successful", token, user.getRole());
    }

    public Map<String, String> forgotPassword(String email) {
        log.info("[AUTH] Forgot-password request for: {}", email);

        userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[AUTH] Forgot-password — no account found for: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No account found with this email address");
                });

        otpService.generateAndSendOtp(email);
        log.info("[AUTH] ✅ OTP generated and dispatched for: {}", email);
        return Map.of("message", "OTP sent to " + email);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Map<String, String> resetPassword(String email, String otp, String newPassword) {
        log.info("[AUTH] Reset-password request for: {}", email);

        if (email == null || email.isBlank() || otp == null || otp.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email, OTP and new password are required");
        }

        if (!otpService.verifyOtp(email, otp)) {
            log.warn("[AUTH] Reset-password failed — invalid or expired OTP for: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        String encoded = passwordEncoder.encode(newPassword);
        log.info("[AUTH] Executing direct DB update for: {}", email);
        int updated = userRepository.updatePassword(email, encoded);

        if (updated == 0) {
            log.warn("[AUTH] Reset-password — no rows updated for: {}", email);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        log.info("[AUTH] ✅ Password updated in DB rows={} for: {}", updated, email);
        otpService.invalidateOtp(email);
        log.info("[AUTH] ✅ Password reset complete for: {}", email);
        return Map.of("message", "Password reset successfully");
    }
}
