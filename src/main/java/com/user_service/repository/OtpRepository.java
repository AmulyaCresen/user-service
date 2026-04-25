package com.user_service.repository;
import com.user_service.model.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface OtpRepository extends JpaRepository<OtpRecord, Long> {
    Optional<OtpRecord> findByEmailId(String emailId);
    void deleteByEmailId(String emailId);
}