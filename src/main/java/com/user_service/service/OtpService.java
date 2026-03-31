package com.user_service.service;

import com.user_service.model.OtpRecord;
import com.user_service.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 10;

    @Transactional
    public void generateAndSendOtp(String email) {
        log.info("[OTP] Request received for: {}", email);

        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpRecord record = otpRepository.findByEmailId(email).orElse(new OtpRecord());
        record.setEmailId(email);
        record.setOtpCode(otp);
        record.setExpiryTime(expiry);
        otpRepository.save(record);
        log.info("[OTP] ✅ OTP saved to mail.otp for: {} | expires at: {}", email, expiry);

        emailService.sendOtpEmail(email, otp);
        log.info("[OTP] Mail dispatched asynchronously for: {}", email);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        log.info("[OTP] Verifying OTP for: {}", email);
        return otpRepository.findByEmailId(email).map(record -> {
            if (LocalDateTime.now().isAfter(record.getExpiryTime())) {
                log.warn("[OTP] OTP expired for: {}", email);
                otpRepository.deleteByEmailId(email);
                return false;
            }
            boolean match = record.getOtpCode().equals(otp);
            log.info("[OTP] OTP match result for {}: {}", email, match);
            return match;
        }).orElseGet(() -> {
            log.warn("[OTP] No OTP record found in DB for: {}", email);
            return false;
        });
    }

    @Transactional
    public void invalidateOtp(String email) {
        log.info("[OTP] Invalidating OTP for: {}", email);
        otpRepository.deleteByEmailId(email);
    }
}
