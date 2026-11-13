package org.delcom.ifs25013springbootcashflow.usecase;

import org.delcom.ifs25013springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25013springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUseCaseTest {

    private UserRepository repository;
    private PasswordHasher hasher;
    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        hasher = new PasswordHasher();
        useCase = new AuthUseCase(repository, hasher);
    }

    @Test
    void registerCreatesUserRoleWithHashedPassword() throws Exception {
        User user = useCase.register(" steven ", "rahasia", " Steven M ");

        assertEquals("steven", user.getUsername());
        assertEquals("Steven M", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertNotEquals("rahasia", user.getPasswordHash());
        assertTrue(hasher.matches("rahasia", user.getPasswordHash()));
    }

    @Test
    void registerRejectsBlankFieldsAndDuplicateUsername() throws Exception {
        assertEquals("Username tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.register(" ", "p", "N")).getMessage());
        assertEquals("Nama lengkap tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.register("u", "p", null)).getMessage());
        assertEquals("Password tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.register("u", "", "N")).getMessage());

        useCase.register("u", "p", "N");
        assertEquals("Username sudah digunakan",
                assertThrows(ValidationException.class, () -> useCase.register("u", "p2", "N2")).getMessage());
    }

    @Test
    void loginReturnsUserOnlyForCorrectCredentials() throws Exception {
        useCase.register("u", "p", "N");

        assertEquals("u", useCase.login(" u ", "p").getUsername());
        assertEquals("Username atau password salah",
                assertThrows(ValidationException.class, () -> useCase.login("u", "salah")).getMessage());
        assertEquals("Username atau password salah",
                assertThrows(ValidationException.class, () -> useCase.login("tidak-ada", "p")).getMessage());
        assertEquals("Username tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.login(null, "p")).getMessage());
        assertEquals("Password tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.login("u", " ")).getMessage());
    }

    @Test
    void ensureRootAdminCreatesAdminOnlyOnce() throws Exception {
        useCase.ensureRootAdmin("admin", "admin");
        useCase.ensureRootAdmin("admin", "lain");

        assertEquals(1, repository.findAll().size());
        User admin = useCase.login("admin", "admin");
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isAdmin());
    }
}
