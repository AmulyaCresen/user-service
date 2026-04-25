package com.user_service.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
@Entity
@Table(name = "user_profile", schema = "code")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_id")
    private String companyId;
    @Column(name = "user_name", unique = true)
    private String userName;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "email_id", unique = true, nullable = false)
    private String email;
    @Column(name = "user_pswd")
    private String password;
    @Column(name = "role")
    private String role;
    @Column(name = "active", nullable = false)
    private boolean active = true;
    @Column(name = "create_date")
    private OffsetDateTime createDate;
    @Column(name = "update_date")
    private OffsetDateTime updateDate;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "last_login")
    private OffsetDateTime lastLogin;
    @Column(name = "gender")
    private String gender;
}