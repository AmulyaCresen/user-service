package com.user_service.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String token;
    private String role;
    private String fullName;
    private String email;
    private String gender;
}