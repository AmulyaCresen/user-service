package com.user_service.dto;
import lombok.Data;
@Data
public class UpdateUserRequest {
    private String userName;
    private String fullName;
    private String role;
    private boolean active;
    private String gender;
}