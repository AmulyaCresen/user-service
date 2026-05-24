package com.user_service.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "role", schema = "code")
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "role_name", unique = true)
    private String roleName;
    @Column(name = "unique_name", unique = true)
    private String uniqueName;
    @Column(name = "role_desc")
    private String roleDesc;
}