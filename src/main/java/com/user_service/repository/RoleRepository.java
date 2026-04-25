package com.user_service.repository;
import com.user_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RoleRepository extends JpaRepository<Role, Long> {
}