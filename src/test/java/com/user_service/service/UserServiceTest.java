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
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @InjectMocks private UserService userService;
    private User mockUser;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setUserName("testuser");
        mockUser.setFullName("Test User");
        mockUser.setRole("EMPLOYEE");
        mockUser.setActive(true);
        mockUser.setGender("Male");
        mockUser.setCompanyId("Cresen1");
        createRequest = new CreateUserRequest();
        createRequest.setUserName("newuser");
        createRequest.setFullName("New User");
        createRequest.setEmail("new@test.com");
        createRequest.setPassword(Base64.getEncoder().encodeToString("Pass@123".getBytes()));
        createRequest.setRole("EMPLOYEE");
        createRequest.setActive(true);
        createRequest.setGender("Female");
        updateRequest = new UpdateUserRequest();
        updateRequest.setUserName("updated");
        updateRequest.setFullName("Updated User");
        updateRequest.setRole("MANAGER");
        updateRequest.setActive(true);
        updateRequest.setGender("Male");
    }
    @Test
    void getAllUsers_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        assertEquals(1, userService.getAllUsers().size());
    }
    @Test
    void getAllRoles_shouldReturnOnlyEmployeeAndManager() {
        Role emp = new Role(); emp.setRoleName("EMPLOYEE");
        Role mgr = new Role(); mgr.setRoleName("MANAGER");
        Role adm = new Role(); adm.setRoleName("ADMIN");
        when(roleRepository.findAll()).thenReturn(List.of(emp, mgr, adm));
        List<Role> result = userService.getAllRoles();
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(r -> r.getRoleName().equals("ADMIN")));
    }
    @Test
    void getMenusByRole_shouldReturnMenus() {
        Menu m = new Menu(); m.setMenuKey("home"); m.setMenuLabel("Home");
        when(menuRepository.findByRoleName("ADMIN")).thenReturn(List.of(m));
        List<Menu> result = userService.getMenusByRole("ADMIN");
        assertEquals(1, result.size());
        assertEquals("home", result.get(0).getMenuKey());
    }
    @Test
    void getStats_shouldReturnCorrectCounts() {
        User inactive = new User(); inactive.setActive(false);
        when(userRepository.findAll()).thenReturn(List.of(mockUser, inactive));
        Map<String, Long> stats = userService.getStats();
        assertEquals(2L, stats.get("totalUsers"));
        assertEquals(1L, stats.get("activeUsers"));
        assertEquals(1L, stats.get("inactiveUsers"));
    }
    @Test
    void getStats_shouldReturnZeros_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());
        Map<String, Long> stats = userService.getStats();
        assertEquals(0L, stats.get("totalUsers"));
    }
    @Test
    void getNextCompanyId_shouldReturnCresen1_whenNoUsersExist() {
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of());
        assertEquals("Cresen1", userService.getNextCompanyId());
    }
    @Test
    void getNextCompanyId_shouldIncrementFromMax() {
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of("Cresen3", "Cresen1", "Cresen2"));
        assertEquals("Cresen4", userService.getNextCompanyId());
    }
    @Test
    void getNextCompanyId_shouldFallback_whenCompanyIdNotNumeric() {
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of("CresenABC", "CresenXYZ"));
        String result = userService.getNextCompanyId();
        assertNotNull(result);
        assertTrue(result.startsWith("Cresen"));
    }
    @Test
    void emailExists_shouldReturnTrue_whenEmailFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        assertTrue(userService.emailExists("test@test.com"));
    }
    @Test
    void emailExists_shouldReturnFalse_whenEmailNotFound() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());
        assertFalse(userService.emailExists("none@test.com"));
    }
    @Test
    void usernameExists_shouldReturnTrue_whenUsernameFound() {
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(mockUser));
        assertTrue(userService.usernameExists("testuser"));
    }
    @Test
    void usernameExists_shouldReturnFalse_whenUsernameNotFound() {
        when(userRepository.findByUserName("nobody")).thenReturn(Optional.empty());
        assertFalse(userService.usernameExists("nobody"));
    }
    @Test
    void createUser_shouldSucceed_whenValidRequest() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(mockUser);
        doNothing().when(emailService).sendWelcomeEmail(any(), any(), any(), any(), any());
        User result = userService.createUser(createRequest, "admin@test.com");
        assertNotNull(result);
        verify(userRepository).save(any());
    }
    @Test
    void createUser_shouldThrow409_whenEmailExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(createRequest, "admin@test.com"));
        assertEquals(409, ex.getStatusCode().value());
    }
    @Test
    void createUser_shouldThrow409_whenUsernameExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(mockUser));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(createRequest, "admin@test.com"));
        assertEquals(409, ex.getStatusCode().value());
    }
    @Test
    void createUser_shouldHandlePlainPassword_whenBase64DecodeFails() {
        createRequest.setPassword("plainpassword");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.findAllCompanyIdsSorted()).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(mockUser);
        doNothing().when(emailService).sendWelcomeEmail(any(), any(), any(), any(), any());
        assertDoesNotThrow(() -> userService.createUser(createRequest, "admin@test.com"));
    }
    @Test
    void createUser_shouldThrow400_whenCreatedByIsBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(createRequest, ""));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void createUser_shouldThrow400_whenRequestIsNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(null, "admin@test.com"));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void updateUser_shouldSucceed_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenReturn(mockUser);
        User result = userService.updateUser(1L, updateRequest, "admin@test.com");
        assertNotNull(result);
        verify(userRepository).save(any());
    }
    @Test
    void updateUser_shouldThrow404_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(99L, updateRequest, "admin@test.com"));
        assertEquals(404, ex.getStatusCode().value());
    }
    @Test
    void deleteUser_shouldSucceed_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        assertDoesNotThrow(() -> userService.deleteUser(1L, "admin@test.com"));
        verify(userRepository).deleteById(1L);
    }
    @Test
    void deleteUser_shouldThrow404_whenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser(99L, "admin@test.com"));
        assertEquals(404, ex.getStatusCode().value());
    }
    @Test
    void getManagers_shouldReturnOnlyActiveManagers() {
        User manager = new User(); manager.setRole("MANAGER"); manager.setActive(true);
        User inactiveManager = new User(); inactiveManager.setRole("MANAGER"); inactiveManager.setActive(false);
        User employee = new User(); employee.setRole("EMPLOYEE"); employee.setActive(true);
        when(userRepository.findAll()).thenReturn(List.of(manager, inactiveManager, employee));
        List<User> result = userService.getManagers();
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }
    @Test
    void getManagers_shouldReturnEmpty_whenNoActiveManagers() {
        User employee = new User(); employee.setRole("EMPLOYEE"); employee.setActive(true);
        when(userRepository.findAll()).thenReturn(List.of(employee));
        assertTrue(userService.getManagers().isEmpty());
    }
    @Test
    void createUser_shouldThrow400_whenCreatedByIsNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(createRequest, null));
        assertEquals(400, ex.getStatusCode().value());
    }
    @Test
    void getFullNameByEmail_shouldReturnName_whenFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        assertTrue(userService.getFullNameByEmail("test@test.com").isPresent());
    }
    @Test
    void getFullNameByEmail_shouldReturnEmpty_whenNotFound() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());
        assertFalse(userService.getFullNameByEmail("none@test.com").isPresent());
    }
}