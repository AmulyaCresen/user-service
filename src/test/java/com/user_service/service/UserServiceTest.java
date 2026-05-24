package com.user_service.service;

import com.user_service.dto.CreateUserRequest;
import com.user_service.dto.UpdateUserRequest;
import com.user_service.model.Menu;
import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.repository.MenuRepository;
import com.user_service.repository.RoleRepository;
import com.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private Menu testMenu;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("EMPLOYEE");
        testUser.setActive(true);
        testUser.setGender("Male");
        testUser.setCompanyId("Cresen1");
        testUser.setCreateDate(OffsetDateTime.now());
        testUser.setCreatedBy("admin");

        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("EMPLOYEE");
        testRole.setRoleDesc("Employee Role");

        testMenu = new Menu();
        testMenu.setId(1L);
        testMenu.setMenuKey("home");
        testMenu.setMenuLabel("Home");
        testMenu.setMenuOrder(1);
        testMenu.setActive(true);
    }

    @Test
    void testGetMenusByRole() {
        when(menuRepository.findByRoleName("EMPLOYEE")).thenReturn(List.of(testMenu));

        List<Menu> menus = userService.getMenusByRole("EMPLOYEE");

        assertNotNull(menus);
        assertEquals(1, menus.size());
        assertEquals("home", menus.get(0).getMenuKey());
        verify(menuRepository).findByRoleName("EMPLOYEE");
    }

    @Test
    void testGetAllRoles() {
        Role employeeRole = new Role();
        employeeRole.setRoleName("EMPLOYEE");
        
        Role managerRole = new Role();
        managerRole.setRoleName("MANAGER");
        
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");

        when(roleRepository.findAll()).thenReturn(List.of(employeeRole, managerRole, adminRole));

        List<Role> roles = userService.getAllRoles();

        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("EMPLOYEE")));
        assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("MANAGER")));
        assertFalse(roles.stream().anyMatch(r -> r.getRoleName().equals("ADMIN")));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("test@example.com", users.get(0).getEmail());
    }

    @Test
    void testGetManagers() {
        User manager = new User();
        manager.setRole("MANAGER");
        manager.setActive(true);

        User inactiveManager = new User();
        inactiveManager.setRole("MANAGER");
        inactiveManager.setActive(false);

        when(userRepository.findAll()).thenReturn(List.of(testUser, manager, inactiveManager));

        List<User> managers = userService.getManagers();

        assertNotNull(managers);
        assertEquals(1, managers.size());
        assertEquals("MANAGER", managers.get(0).getRole());
        assertTrue(managers.get(0).isActive());
    }

    @Test
    void testGetFullNameByEmail_Found() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<String> fullName = userService.getFullNameByEmail("test@example.com");

        assertTrue(fullName.isPresent());
        assertEquals("Test User", fullName.get());
    }

    @Test
    void testGetFullNameByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<String> fullName = userService.getFullNameByEmail("notfound@example.com");

        assertFalse(fullName.isPresent());
    }

    @Test
    void testEmailExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertTrue(userService.emailExists("test@example.com"));
        assertFalse(userService.emailExists("notfound@example.com"));
    }

    @Test
    void testUsernameExists() {
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUserName("notfound")).thenReturn(Optional.empty());

        assertTrue(userService.usernameExists("testuser"));
        assertFalse(userService.usernameExists("notfound"));
    }

    @Test
    void testGetStats() {
        User activeUser = new User();
        activeUser.setActive(true);

        User inactiveUser = new User();
        inactiveUser.setActive(false);

        when(userRepository.findAll()).thenReturn(List.of(testUser, activeUser, inactiveUser));

        Map<String, Long> stats = userService.getStats();

        assertNotNull(stats);
        assertEquals(3L, stats.get("totalUsers"));
        assertEquals(2L, stats.get("activeUsers"));
        assertEquals(1L, stats.get("inactiveUsers"));
    }

    @Test
    void testGetNextCompanyId_FirstUser() {
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of());

        String companyId = userService.getNextCompanyId();

        assertEquals("Cresen1", companyId);
    }

    @Test
    void testGetNextCompanyId_ExistingUsers() {
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of("Cresen1", "Cresen2", "Cresen3"));

        String companyId = userService.getNextCompanyId();

        assertEquals("Cresen4", companyId);
    }

    @Test
    void testGetNextCompanyId_InvalidFormat() {
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of("InvalidId1", "InvalidId2"));

        String companyId = userService.getNextCompanyId();

        assertEquals("Cresen3", companyId);
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(1L, "admin"));

        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1L, "admin"));
    }

    @Test
    void testUpdateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUserName("updateduser");
        request.setFullName("Updated User");
        request.setRole("MANAGER");
        request.setActive(false);
        request.setGender("Female");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updated = userService.updateUser(1L, request, "admin");

        assertNotNull(updated);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUserName("updateduser");
        request.setFullName("Updated User");
        request.setRole("MANAGER");
        request.setActive(false);
        request.setGender("Female");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.updateUser(1L, request, "admin"));
    }

    @Test
    void testCreateUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("newuser");
        request.setFullName("New User");
        request.setEmail("newuser@example.com");
        request.setPassword(Base64.getEncoder().encodeToString("password123".getBytes()));
        request.setRole("EMPLOYEE");
        request.setActive(true);
        request.setGender("Male");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of("Cresen1"));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString(), anyString(), anyString(), anyString());

        User created = userService.createUser(request, "admin");

        assertNotNull(created);
        verify(userRepository).save(any(User.class));
        verify(emailService).sendWelcomeEmail(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testCreateUser_WithPlainPassword() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("newuser");
        request.setFullName("New User");
        request.setEmail("newuser@example.com");
        request.setPassword("plainPassword");
        request.setRole("EMPLOYEE");
        request.setActive(true);
        request.setGender("Male");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString(), anyString(), anyString(), anyString());

        User created = userService.createUser(request, "admin");

        assertNotNull(created);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_NullRequest() {
        assertThrows(ResponseStatusException.class, () -> userService.createUser(null, "admin"));
    }

    @Test
    void testCreateUser_BlankCreatedBy() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("newuser");
        request.setFullName("New User");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setRole("EMPLOYEE");
        request.setActive(true);
        request.setGender("Male");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(request, ""));
        assertThrows(ResponseStatusException.class, () -> userService.createUser(request, null));
    }

    @Test
    void testCreateUser_EmailExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("newuser");
        request.setFullName("New User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("EMPLOYEE");
        request.setActive(true);
        request.setGender("Male");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(ResponseStatusException.class, () -> userService.createUser(request, "admin"));
    }

    @Test
    void testCreateUser_UsernameExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("testuser");
        request.setFullName("New User");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setRole("EMPLOYEE");
        request.setActive(true);
        request.setGender("Male");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(ResponseStatusException.class, () -> userService.createUser(request, "admin"));
    }
}
