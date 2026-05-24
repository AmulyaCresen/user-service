package com.user_service.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    @Test
    void user_gettersAndSetters() {
        User user = new User();
        OffsetDateTime now = OffsetDateTime.now();

        user.setId(1L);
        user.setCompanyId("Cresen1");
        user.setUserName("testuser");
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole("EMPLOYEE");
        user.setActive(true);
        user.setGender("Male");
        user.setCreateDate(now);
        user.setUpdateDate(now);
        user.setCreatedBy("admin");
        user.setUpdatedBy("admin");
        user.setLastLogin(now);

        assertEquals(1L, user.getId());
        assertEquals("Cresen1", user.getCompanyId());
        assertEquals("testuser", user.getUserName());
        assertEquals("Test User", user.getFullName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("EMPLOYEE", user.getRole());
        assertTrue(user.isActive());
        assertEquals("Male", user.getGender());
        assertEquals(now, user.getCreateDate());
        assertEquals(now, user.getUpdateDate());
        assertEquals("admin", user.getCreatedBy());
        assertEquals("admin", user.getUpdatedBy());
        assertEquals(now, user.getLastLogin());
    }

    @Test
    void user_defaultConstructor_activeIsTrue() {
        User user = new User();
        assertTrue(user.isActive());
    }

    @Test
    void user_setActiveToFalse() {
        User user = new User();
        user.setActive(false);
        assertFalse(user.isActive());
    }

    @Test
    void menu_gettersAndSetters() {
        Menu menu = new Menu();
        menu.setId(10L);
        menu.setMenuKey("dashboard");
        menu.setMenuLabel("Dashboard");
        menu.setMenuOrder(1);
        menu.setActive(true);
        menu.setRoleId(2L);

        assertEquals(10L, menu.getId());
        assertEquals("dashboard", menu.getMenuKey());
        assertEquals("Dashboard", menu.getMenuLabel());
        assertEquals(1, menu.getMenuOrder());
        assertTrue(menu.isActive());
        assertEquals(2L, menu.getRoleId());
    }

    @Test
    void menu_defaultConstructor_activeIsTrue() {
        Menu menu = new Menu();
        assertTrue(menu.isActive());
    }

    @Test
    void role_gettersAndSetters() {
        Role role = new Role();
        role.setId(5L);
        role.setRoleName("MANAGER");
        role.setUniqueName("manager");
        role.setRoleDesc("Manager role description");

        assertEquals(5L, role.getId());
        assertEquals("MANAGER", role.getRoleName());
        assertEquals("manager", role.getUniqueName());
        assertEquals("Manager role description", role.getRoleDesc());
    }

    @Test
    void otpRecord_gettersAndSetters() {
        OtpRecord record = new OtpRecord();
        java.time.LocalDateTime expiry = java.time.LocalDateTime.now().plusMinutes(10);

        record.setId(99L);
        record.setEmailId("user@example.com");
        record.setOtpCode("123456");
        record.setExpiryTime(expiry);

        assertEquals(99L, record.getId());
        assertEquals("user@example.com", record.getEmailId());
        assertEquals("123456", record.getOtpCode());
        assertEquals(expiry, record.getExpiryTime());
    }

    @Test
    void otpRecord_defaultConstructor() {
        OtpRecord record = new OtpRecord();
        assertNull(record.getId());
        assertNull(record.getEmailId());
        assertNull(record.getOtpCode());
    }
}
