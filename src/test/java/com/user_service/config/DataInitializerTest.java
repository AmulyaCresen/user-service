package com.user_service.config;
import com.user_service.model.Menu;
import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.repository.MenuRepository;
import com.user_service.repository.RoleRepository;
import com.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class DataInitializerTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MenuRepository menuRepository;
    @Mock private RoleRepository roleRepository;
    @InjectMocks private DataInitializer dataInitializer;

    @Test
    void run_shouldSeedUsers_whenNoneExist() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(new User());
        Role role = new Role(); role.setId(1L); role.setRoleName("ADMIN");
        when(roleRepository.findAll()).thenReturn(List.of(role));
        Menu auditMenu = new Menu(); auditMenu.setMenuKey("audit");
        when(menuRepository.findByRoleName(anyString())).thenReturn(List.of(auditMenu));
        dataInitializer.run();
        verify(userRepository, times(3)).save(any());
    }

    @Test
    void run_shouldUpdateCompanyId_whenDifferent() throws Exception {
        User user = new User(); user.setCompanyId("C001");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        Role role = new Role(); role.setId(1L); role.setRoleName("ADMIN");
        when(roleRepository.findAll()).thenReturn(List.of(role));
        Menu auditMenu = new Menu(); auditMenu.setMenuKey("audit");
        when(menuRepository.findByRoleName(anyString())).thenReturn(List.of(auditMenu));
        dataInitializer.run();
        verify(userRepository, atLeast(3)).save(any());
    }

    @Test
    void run_shouldNotUpdateCompanyId_whenAlreadyCorrect() throws Exception {
        User admin = new User(); admin.setCompanyId("Cresen1");
        User manager = new User(); manager.setCompanyId("Cresen2");
        User employee = new User(); employee.setCompanyId("Cresen3");
        when(userRepository.findByEmail("amulyachodisetty2004@gmail.com")).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("amulyachodisetty0@gmail.com")).thenReturn(Optional.of(manager));
        when(userRepository.findByEmail("nagaamulyachodisetty@gmail.com")).thenReturn(Optional.of(employee));
        Role role = new Role(); role.setId(1L); role.setRoleName("ADMIN");
        when(roleRepository.findAll()).thenReturn(List.of(role));
        Menu auditMenu = new Menu(); auditMenu.setMenuKey("audit");
        when(menuRepository.findByRoleName(anyString())).thenReturn(List.of(auditMenu));
        dataInitializer.run();
        verify(userRepository, never()).save(any());
    }

    @Test
    void run_shouldSeedAuditMenu_whenNotExists() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(new User());
        Role role = new Role(); role.setId(1L); role.setRoleName("ADMIN");
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(menuRepository.findByRoleName(anyString())).thenReturn(List.of());
        when(menuRepository.save(any())).thenReturn(new Menu());
        dataInitializer.run();
        verify(menuRepository, atLeastOnce()).save(any());
    }

    @Test
    void run_shouldNotSeedAuditMenu_whenRoleNotFound() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(new User());
        when(roleRepository.findAll()).thenReturn(List.of());
        dataInitializer.run();
        verify(menuRepository, never()).save(any());
    }
}