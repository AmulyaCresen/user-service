package com.user_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp", schema = "mail")
@Getter
@Setter
@NoArgsConstructor
public class OtpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_id", unique = true, nullable = false)
    private String emailId;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
}
