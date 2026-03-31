package com.user_service.service;

import com.user_service.model.OtpRecord;
import com.user_service.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock private OtpRepository otpRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private OtpService otpService;

    private OtpRecord validRecord;

    @BeforeEach
    void setUp() {
        validRecord = new OtpRecord();
        validRecord.setEmailId("test@test.com");
        validRecord.setOtpCode("123456");
        validRecord.setExpiryTime(LocalDateTime.now().plusMinutes(5));
    }


    @Test
    void generateAndSendOtp_shouldSaveAndSendEmail() {
        when(otpRepository.findByEmailId("test@test.com")).thenReturn(Optional.empty());
        when(otpRepository.save(any())).thenReturn(validRecord);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        otpService.generateAndSendOtp("test@test.com");

        verify(otpRepository).save(any(OtpRecord.class));
        verify(emailService).sendOtpEmail(eq("test@test.com"), anyString());
    }

    @Test
    void generateAndSendOtp_shouldUpdateExistingRecord_whenOtpAlreadyExists() {
        when(otpRepository.findByEmailId("test@test.com")).thenReturn(Optional.of(validRecord));
        when(otpRepository.save(any())).thenReturn(validRecord);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        otpService.generateAndSendOtp("test@test.com");

        ArgumentCaptor<OtpRecord> captor = ArgumentCaptor.forClass(OtpRecord.class);
        verify(otpRepository).save(captor.capture());
        assertEquals("test@test.com", captor.getValue().getEmailId());
    }

    @Test
    void generateAndSendOtp_shouldGenerate6DigitOtp() {
        when(otpRepository.findByEmailId("test@test.com")).thenReturn(Optional.empty());
        when(otpRepository.save(any())).thenReturn(validRecord);
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());

        otpService.generateAndSendOtp("test@test.com");

        ArgumentCaptor<OtpRecord> captor = ArgumentCaptor.forClass(OtpRecord.class);
        verify(otpRepository).save(captor.capture());
        String otp = captor.getValue().getOtpCode();
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }


    @Test
    void verifyOtp_shouldReturnTrue_whenOtpMatchesAndNotExpired() {
        when(otpRepository.findByEmailId("test@test.com")).thenReturn(Optional.of(validRecord));

        assertTrue(otpService.verifyOtp("test@test.com", "123456"));
    }

    @Test
    void verifyOtp_shouldReturnFalse_whenOtpDoesNotMatch() {
        when(otpRepository.findByEmailId("test@test.com")).thenReturn(Optional.of(validRecord));

        assertFalse(otpService.verifyOtp("test@test.com", "999999"));
    }

    @Test
    void verifyOtp_shouldReturnFalse_whenOtpExpired() {
        validRecord.setExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(otpRepository.findByEmailId("test@test.com")).thenReturn(Optional.of(validRecord));
        doNothing().when(otpRepository).deleteByEmailId("test@test.com");

        assertFalse(otpService.verifyOtp("test@test.com", "123456"));
        verify(otpRepository).deleteByEmailId("test@test.com");
    }

    @Test
    void verifyOtp_shouldReturnFalse_whenNoRecordFound() {
        when(otpRepository.findByEmailId("test@test.com")).thenReturn(Optional.empty());

        assertFalse(otpService.verifyOtp("test@test.com", "123456"));
    }


    @Test
    void invalidateOtp_shouldDeleteRecord() {
        doNothing().when(otpRepository).deleteByEmailId("test@test.com");

        otpService.invalidateOtp("test@test.com");

        verify(otpRepository).deleteByEmailId("test@test.com");
    }
}
