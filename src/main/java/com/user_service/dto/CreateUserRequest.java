package com.user_service.dto;
import lombok.Data;
@Data
public class CreateUserRequest {
    private String userName;
    private String fullName;
    private String email;
    private String password;
    private String role;
    private boolean active = true;
    private String gender;
}