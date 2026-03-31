package com.user_service.config;

import com.user_service.model.User;
import com.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("amulyachodisetty2004@gmail.com", "Reset@123", "ADMIN",    "H N Amulya",  "C001");
        seedUser("amulyachodisetty0@gmail.com",    "Reset@123", "MANAGER",  "Amulya",      "C001");
        seedUser("nagaamulyachodisetty@gmail.com",  "Reset@123", "EMPLOYEE", "Ch Amulya",   "C001");
    }

    private void seedUser(String email, String rawPassword, String role, String fullName, String companyId) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setUserName(email);
            user.setFullName(fullName);
            user.setCompanyId(companyId);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setActive(true);
            user.setCreateDate(OffsetDateTime.now());
            user.setCreatedBy("system");
            userRepository.save(user);
            log.info("[DataInitializer] Created user: {} ({})", email, role);
        }
    }
}
