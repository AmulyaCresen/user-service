package com.user_service.controller;
import com.user_service.dto.CreateUserRequest;
import com.user_service.dto.UpdateUserRequest;
import com.user_service.model.Menu;
import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/fullname")
    public ResponseEntity<String> getFullName(@RequestParam String email) {
        return userService.getFullNameByEmail(email)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok(email));
    }
    @GetMapping("/menus/{roleName}")
    public ResponseEntity<List<Menu>> getMenusByRole(@PathVariable String roleName) {
        return ResponseEntity.ok(userService.getMenusByRole(roleName));
    }
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/managers")
    public ResponseEntity<List<User>> getManagers() {
        return ResponseEntity.ok(userService.getManagers());
    }
    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Long>> getStats() {
        return ResponseEntity.ok(userService.getStats());
    }
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.emailExists(email));
    }
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.usernameExists(username));
    }
    @GetMapping(value = "/next-company-id", produces = "text/plain")
    public ResponseEntity<String> getNextCompanyId() {
        return ResponseEntity.ok(userService.getNextCompanyId());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String deletedBy) {
        userService.deleteUser(id, deletedBy);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            @RequestHeader("X-User-Email") String updatedBy) {
        return ResponseEntity.ok(userService.updateUser(id, request, updatedBy));
    }
    @PostMapping("/create")
    public ResponseEntity<User> createUser(
            @RequestBody CreateUserRequest request,
            @RequestHeader("X-User-Email") String createdBy) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request, createdBy));
    }
}