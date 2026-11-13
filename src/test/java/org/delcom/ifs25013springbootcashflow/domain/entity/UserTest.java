package org.delcom.ifs25013springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void exposesAllFieldsAndRole() {
        User admin = new User(1, "admin", "hash", "Administrator", Role.ADMIN, "foto.png");
        User user = new User(2, "steven", "hash2", "Steven", Role.USER, null);

        assertEquals(1, admin.getId());
        assertEquals("admin", admin.getUsername());
        assertEquals("hash", admin.getPasswordHash());
        assertEquals("Administrator", admin.getFullName());
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("foto.png", admin.getPhotoPath());
        assertTrue(admin.isAdmin());
        assertFalse(user.isAdmin());
        assertNull(user.getPhotoPath());
    }

    @Test
    void changeMethodsUpdateMutableFields() {
        User user = new User(2, "steven", "hash", "Steven", Role.USER, null);

        user.changeFullName("Steven Manurung");
        user.changePasswordHash("hash-baru");
        user.changePhotoPath("baru.png");

        assertEquals("Steven Manurung", user.getFullName());
        assertEquals("hash-baru", user.getPasswordHash());
        assertEquals("baru.png", user.getPhotoPath());
    }
}
