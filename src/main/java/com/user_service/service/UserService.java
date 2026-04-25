package com.user_service.service;
import com.user_service.dto.CreateUserRequest;
import com.user_service.dto.UpdateUserRequest;
import com.user_service.model.Menu;
import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.repository.MenuRepository;
import com.user_service.repository.RoleRepository;
import com.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String COMPANY_PREFIX = "Cresen";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    public List<Menu> getMenusByRole(String roleName) {
        return menuRepository.findByRoleName(roleName);
    }
    public List<Role> getAllRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equals("EMPLOYEE") || r.getRoleName().equals("MANAGER"))
                .toList();
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public List<User> getManagers() {
        return userRepository.findAll().stream()
                .filter(u -> "MANAGER".equalsIgnoreCase(u.getRole()) && u.isActive())
                .toList();
    }
    public java.util.Optional<String> getFullNameByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getFullName);
    }
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    public boolean usernameExists(String username) {
        return userRepository.findByUserName(username).isPresent();
    }
    public java.util.Map<String, Long> getStats() {
        List<User> users = userRepository.findAll();
        long total = users.size();
        long active = users.stream().filter(User::isActive).count();
        long inactive = total - active;
        return java.util.Map.of(
                "totalUsers", total,
                "activeUsers", active,
                "inactiveUsers", inactive
        );
    }
    public String getNextCompanyId() {
        return generateNextCompanyId();
    }
    private String generateNextCompanyId() {
        List<String> ids = userRepository.findAllCompanyIdsSorted();
        if (ids.isEmpty()) return COMPANY_PREFIX + 1;
        try {
            int max = ids.stream()
                    .map(id -> Integer.parseInt(id.replace(COMPANY_PREFIX, "")))
                    .max(Integer::compareTo)
                    .orElse(0);
            return COMPANY_PREFIX + (max + 1);
        } catch (NumberFormatException e) {
            return COMPANY_PREFIX + (ids.size() + 1);
        }
    }
    @Transactional
    public void deleteUser(Long id, String deletedBy) {
        if (!userRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        userRepository.deleteById(id);
        log.info("[USER] Deleted user id={} by {}", id, deletedBy);
    }
    @Transactional
    public User updateUser(Long id, UpdateUserRequest request, String updatedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setUserName(request.getUserName());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setActive(request.isActive());
        user.setGender(request.getGender());
        user.setUpdateDate(OffsetDateTime.now());
        user.setUpdatedBy(updatedBy);
        User saved = userRepository.save(user);
        log.info("[USER] Updated user id={} by {}", id, updatedBy);
        return saved;
    }
    public User createUser(CreateUserRequest request, String createdBy) {
        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot be null");
        if (createdBy == null || createdBy.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CreatedBy cannot be blank");
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        String decodedPassword;
        try {
            decodedPassword = new String(Base64.getDecoder().decode(request.getPassword()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            decodedPassword = request.getPassword();
        }
        String companyId = generateNextCompanyId();
        User user = new User();
        user.setUserName(request.getUserName());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(decodedPassword));
        user.setRole(request.getRole());
        user.setActive(request.isActive());
        user.setGender(request.getGender());
        user.setCompanyId(companyId);
        user.setCreateDate(OffsetDateTime.now());
        user.setCreatedBy(createdBy);
        User saved = userRepository.save(user);
        log.info("[USER] Created user: {} | companyId: {}", saved.getEmail(), companyId);
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName(), saved.getUserName(), companyId, decodedPassword);
        return saved;
    }
}