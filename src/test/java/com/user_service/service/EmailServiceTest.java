package com.user_service.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock private JavaMailSender mailSender;
    @InjectMocks private EmailService emailService;
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "test@test.com");
    }
    @Test
    void sendOtpEmail_shouldSendSuccessfully() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendOtpEmail("user@test.com", "123456"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    @Test
    void sendOtpEmail_shouldHandleMailException() {
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendOtpEmail("user@test.com", "123456"));
    }
    @Test
    void sendWelcomeEmail_shouldSendSuccessfully() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(
                "user@test.com", "Test User", "testuser", "Cresen1", "Pass@123"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    @Test
    void sendWelcomeEmail_shouldHandleMailException() {
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(
                "user@test.com", "Test User", "testuser", "Cresen1", "Pass@123"));
    }
}