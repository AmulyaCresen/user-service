package com.user_service.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_service.dto.CreateUserRequest;
import com.user_service.dto.UpdateUserRequest;
import com.user_service.model.Menu;
import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private UserService userService;
    @Test
    @WithMockUser
    void getAllUsers_shouldReturn200() throws Exception {
        User u = new User(); u.setEmail("test@test.com");
        when(userService.getAllUsers()).thenReturn(List.of(u));
        mockMvc.perform(get("/users/all"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser
    void getStats_shouldReturn200() throws Exception {
        when(userService.getStats()).thenReturn(Map.of("totalUsers", 3L, "activeUsers", 2L, "inactiveUsers", 1L));
        mockMvc.perform(get("/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3));
    }
    @Test
    @WithMockUser
    void getMenusByRole_shouldReturn200() throws Exception {
        Menu m = new Menu(); m.setMenuKey("home");
        when(userService.getMenusByRole("ADMIN")).thenReturn(List.of(m));
        mockMvc.perform(get("/users/menus/ADMIN"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser
    void getRoles_shouldReturn200() throws Exception {
        Role r = new Role(); r.setRoleName("EMPLOYEE");
        when(userService.getAllRoles()).thenReturn(List.of(r));
        mockMvc.perform(get("/users/roles"))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser
    void checkEmail_shouldReturnTrue() throws Exception {
        when(userService.emailExists("test@test.com")).thenReturn(true);
        mockMvc.perform(get("/users/check-email").param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
    @Test
    @WithMockUser
    void getNextCompanyId_shouldReturn200() throws Exception {
        when(userService.getNextCompanyId()).thenReturn("Cresen4");
        mockMvc.perform(get("/users/next-company-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cresen4"));
    }
    @Test
    @WithMockUser
    void createUser_shouldReturn201() throws Exception {
        User saved = new User(); saved.setEmail("new@test.com");
        when(userService.createUser(any(), anyString())).thenReturn(saved);
        CreateUserRequest req = new CreateUserRequest();
        req.setUserName("newuser"); req.setFullName("New User");
        req.setEmail("new@test.com"); req.setPassword("UGFzc0AxMjM=");
        req.setRole("EMPLOYEE"); req.setGender("Male");
        mockMvc.perform(post("/users/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "admin@test.com")
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
    @Test
    @WithMockUser
    void updateUser_shouldReturn200() throws Exception {
        User updated = new User(); updated.setFullName("Updated");
        when(userService.updateUser(anyLong(), any(), anyString())).thenReturn(updated);
        UpdateUserRequest req = new UpdateUserRequest();
        req.setUserName("u"); req.setFullName("Updated");
        req.setRole("MANAGER"); req.setGender("Male"); req.setActive(true);
        mockMvc.perform(put("/users/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "admin@test.com")
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser
    void deleteUser_shouldReturn204() throws Exception {
        doNothing().when(userService).deleteUser(anyLong(), anyString());
        mockMvc.perform(delete("/users/1").with(csrf())
                .header("X-User-Email", "admin@test.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void checkUsername_shouldReturnFalse() throws Exception {
        when(userService.usernameExists("nobody")).thenReturn(false);
        mockMvc.perform(get("/users/check-username").param("username", "nobody"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @WithMockUser
    void getManagers_shouldReturn200() throws Exception {
        User m = new User(); m.setRole("MANAGER"); m.setActive(true);
        when(userService.getManagers()).thenReturn(List.of(m));
        mockMvc.perform(get("/users/managers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getFullName_shouldReturn200_whenFound() throws Exception {
        when(userService.getFullNameByEmail("test@test.com")).thenReturn(java.util.Optional.of("Test User"));
        mockMvc.perform(get("/users/fullname").param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Test User"));
    }

    @Test
    @WithMockUser
    void getFullName_shouldReturnEmail_whenNotFound() throws Exception {
        when(userService.getFullNameByEmail("none@test.com")).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/users/fullname").param("email", "none@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("none@test.com"));
    }

    @Test
    @WithMockUser
    void createUser_shouldReturn409_whenEmailExists() throws Exception {
        when(userService.createUser(any(), anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Email already exists"));
        CreateUserRequest req = new CreateUserRequest();
        req.setUserName("u"); req.setFullName("F"); req.setEmail("dup@test.com");
        req.setPassword("UGFzc0AxMjM="); req.setRole("EMPLOYEE"); req.setGender("Male");
        mockMvc.perform(post("/users/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "admin@test.com")
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void updateUser_shouldReturn404_whenNotFound() throws Exception {
        when(userService.updateUser(anyLong(), any(), anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        UpdateUserRequest req = new UpdateUserRequest();
        req.setUserName("u"); req.setFullName("F"); req.setRole("EMPLOYEE"); req.setGender("Male"); req.setActive(true);
        mockMvc.perform(put("/users/99").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Email", "admin@test.com")
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}