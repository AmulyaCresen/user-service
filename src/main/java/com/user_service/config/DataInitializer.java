package com.user_service.config;
import com.user_service.model.Menu;
import com.user_service.model.User;
import com.user_service.repository.MenuRepository;
import com.user_service.repository.RoleRepository;
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
    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;
    @Override
    public void run(String... args) {
        seedUser("amulyachodisetty2004@gmail.com", "Reset@123", "ADMIN",    "H N Amulya",  "Cresen1");
        seedUser("amulyachodisetty0@gmail.com",    "Reset@123", "MANAGER",  "Amulya",      "Cresen2");
        seedUser("nagaamulyachodisetty@gmail.com",  "Reset@123", "EMPLOYEE", "Ch Amulya",   "Cresen3");
        updateCompanyId("amulyachodisetty2004@gmail.com", "Cresen1");
        updateCompanyId("amulyachodisetty0@gmail.com",    "Cresen2");
        updateCompanyId("nagaamulyachodisetty@gmail.com",  "Cresen3");
        seedAuditMenu("ADMIN");
        seedAuditMenu("MANAGER");
        seedAuditMenu("EMPLOYEE");
    }
    private void updateCompanyId(String email, String companyId) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!companyId.equals(user.getCompanyId())) {
                user.setCompanyId(companyId);
                userRepository.save(user);
                log.info("[DataInitializer] Updated companyId for: {} -> {}", email, companyId);
            }
        });
    }
    private void seedAuditMenu(String roleName) {
        roleRepository.findAll().stream()
            .filter(r -> r.getRoleName().equalsIgnoreCase(roleName))
            .findFirst()
            .ifPresent(role -> {
                boolean exists = menuRepository.findByRoleName(roleName).stream()
                    .anyMatch(m -> "audit".equals(m.getMenuKey()));
                if (!exists) {
                    Menu menu = new Menu();
                    menu.setMenuKey("audit");
                    menu.setMenuLabel("Audit Trail");
                    menu.setMenuOrder(99);
                    menu.setActive(true);
                    menu.setRoleId(role.getId());
                    menuRepository.save(menu);
                    log.info("[DataInitializer] Seeded audit menu for role: {}", roleName);
                }
            });
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