package com.user_service.repository;
import com.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String userName);
    List<User> findByRoleIgnoreCaseAndActiveTrue(String role);
    long countByActiveTrue();
    long countByActiveFalse();
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    int updatePassword(@Param("email") String email, @Param("password") String password);
    @Query("SELECT u.companyId FROM User u WHERE u.companyId LIKE 'Cresen%' ORDER BY LENGTH(u.companyId) DESC, u.companyId DESC")
    List<String> findAllCompanyIdsSorted();
}
