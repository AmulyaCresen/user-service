package com.user_service.service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String senderEmail;
    @Async
    public void sendOtpEmail(String email, String otp) {
        log.info("[EMAIL] Async thread started — sending OTP to: {}", email);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(email);
            message.setSubject("Leave Management System – Password Reset OTP");
            message.setText(
                    "Hello,\n\n" +
                    "Your OTP for password reset is: " + otp + "\n\n" +
                    "This OTP is valid for 10 minutes. Do not share it with anyone.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Regards,\nLeave Management System"
            );
            mailSender.send(message);
            log.info("[EMAIL]  OTP email successfully sent to: {}", email);
        } catch (MailException e) {
            log.error("[EMAIL]  Failed to send OTP email to: {} | Reason: {}", email, e.getMessage(), e);
        }
    }
    @Async
    public void sendWelcomeEmail(String email, String fullName, String userName, String companyId, String rawPassword) {
        log.info("[EMAIL] Sending welcome email to: {}", email);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(email);
            message.setSubject("Welcome to Leave Management System – Account Created");
            message.setText(
                    "Hello " + fullName + ",\n\n" +
                    "Your account has been successfully created on the Leave Management System.\n\n" +
                    "Here are your login details:\n" +
                    "  Company ID : " + companyId + "\n" +
                    "  Username   : " + userName + "\n" +
                    "  Password   : " + rawPassword + "\n\n" +
                    "For security reasons, we recommend updating your password after your first login.\n" +
                    "You can update your password by clicking 'Forgot Password' on the login page.\n\n" +
                    "Login URL: http://localhost:4200\n\n" +
                    "Regards,\nLeave Management System"
            );
            mailSender.send(message);
            log.info("[EMAIL]  Welcome email sent to: {}", email);
        } catch (MailException e) {
            log.error("[EMAIL]  Failed to send welcome email to: {} | Reason: {}", email, e.getMessage(), e);
        }
    }
}
