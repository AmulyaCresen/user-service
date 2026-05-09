package com.user_service.controller;

import com.user_service.dto.CreateUserRequest;
import com.user_service.dto.UpdateUserRequest;
import com.user_service.model.Menu;
import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
        testUser.setRole("EMPLOYEE");
        testUser.setActive(true);
        testUser.setGender("Male");
        testUser.setCompanyId("Cresen1");
        testUser.setCreateDate(OffsetDateTime.now());

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
    void testGetMenusByRole() throws Exception {
        when(userService.getMenusByRole("EMPLOYEE")).thenReturn(List.of(testMenu));

        mockMvc.perform(get("/users/menus/EMPLOYEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].menuKey").value("home"));
    }

    @Test
    void testGetAllRoles() throws Exception {
        when(userService.getAllRoles()).thenReturn(List.of(testRole));

        mockMvc.perform(get("/users/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roleName").value("EMPLOYEE"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void testGetManagers() throws Exception {
        User manager = new User();
        manager.setRole("MANAGER");
        manager.setActive(true);

        when(userService.getManagers()).thenReturn(List.of(manager));

        mockMvc.perform(get("/users/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("MANAGER"));
    }

    @Test
    void testGetFullNameByEmail_Found() throws Exception {
        when(userService.getFullNameByEmail("test@example.com")).thenReturn(Optional.of("Test User"));

        mockMvc.perform(get("/users/fullname").param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Test User"));
    }

    @Test
    void testGetFullNameByEmail_NotFound() throws Exception {
        when(userService.getFullNameByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // controller returns ok(email) when not found
        mockMvc.perform(get("/users/fullname").param("email", "notfound@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("notfound@example.com"));
    }

    @Test
    void testCheckEmail_Exists() throws Exception {
        when(userService.emailExists("test@example.com")).thenReturn(true);

        mockMvc.perform(get("/users/check-email").param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCheckEmail_NotExists() throws Exception {
        when(userService.emailExists("other@example.com")).thenReturn(false);

        mockMvc.perform(get("/users/check-email").param("email", "other@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testCheckUsername_Exists() throws Exception {
        when(userService.usernameExists("testuser")).thenReturn(true);

        mockMvc.perform(get("/users/check-username").param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCheckUsername_NotExists() throws Exception {
        when(userService.usernameExists("unknown")).thenReturn(false);

        mockMvc.perform(get("/users/check-username").param("username", "unknown"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testGetStats() throws Exception {
        Map<String, Long> stats = Map.of(
            "totalUsers", 10L,
            "activeUsers", 8L,
            "inactiveUsers", 2L
        );

        when(userService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.activeUsers").value(8))
                .andExpect(jsonPath("$.inactiveUsers").value(2));
    }

    @Test
    void testGetNextCompanyId() throws Exception {
        when(userService.getNextCompanyId()).thenReturn("Cresen5");

        mockMvc.perform(get("/users/next-company-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cresen5"));
    }

    @Test
    void testCreateUser() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class), anyString())).thenReturn(testUser);

        mockMvc.perform(post("/users/create")
                .header("X-User-Email", "admin@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"testuser\",\"fullName\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"cGFzc3dvcmQxMjM=\",\"role\":\"EMPLOYEE\",\"active\":true,\"gender\":\"Male\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testUpdateUser() throws Exception {
        when(userService.updateUser(anyLong(), any(UpdateUserRequest.class), anyString())).thenReturn(testUser);

        mockMvc.perform(put("/users/1")
                .header("X-User-Email", "admin@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"updateduser\",\"fullName\":\"Updated User\",\"role\":\"MANAGER\",\"active\":true,\"gender\":\"Male\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(anyLong(), anyString());

        mockMvc.perform(delete("/users/1")
                .header("X-User-Email", "admin@example.com"))
                .andExpect(status().isNoContent());
    }
}
