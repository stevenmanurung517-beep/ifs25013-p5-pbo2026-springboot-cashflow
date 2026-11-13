package org.delcom.ifs25013springbootcashflow.usecase;

import org.delcom.ifs25013springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25013springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserUseCaseTest {

    private PasswordHasher hasher;
    private UserUseCase useCase;
    private User user;

    @BeforeEach
    void setUp() {
        UserRepository repository = new UserRepository();
        hasher = new PasswordHasher();
        useCase = new UserUseCase(repository, hasher);
        user = repository.save("u", hasher.hash("lama"), "Nama Lama", Role.USER);
    }

    @Test
    void listsAndGetsUsers() throws Exception {
        assertEquals(1, useCase.getAllUsers().size());
        assertEquals("u", useCase.getUser(user.getId()).getUsername());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.getUser(99));
        assertEquals("Pengguna dengan ID 99 tidak ditemukan.", ex.getMessage());
    }

    @Test
    void updateProfileChangesNameAndOptionallyPassword() throws Exception {
        useCase.updateProfile(user.getId(), " Nama Baru ", null);
        assertEquals("Nama Baru", user.getFullName());
        assertTrue(hasher.matches("lama", user.getPasswordHash()));

        useCase.updateProfile(user.getId(), "Nama Baru", "   ");
        assertTrue(hasher.matches("lama", user.getPasswordHash()));

        useCase.updateProfile(user.getId(), "Nama Baru", "baru");
        assertTrue(hasher.matches("baru", user.getPasswordHash()));
    }

    @Test
    void updateProfileRejectsBlankNameAndUnknownUser() {
        assertEquals("Nama lengkap tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), " ", "x")).getMessage());
        assertEquals("Nama Lama", user.getFullName());
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(99, "N", null));
    }

    @Test
    void updatePhotoSetsAndClearsPath() throws Exception {
        useCase.updatePhoto(user.getId(), "foto.png");
        assertEquals("foto.png", user.getPhotoPath());

        useCase.updatePhoto(user.getId(), null);
        assertNull(user.getPhotoPath());

        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(99, "x.png"));
    }
}
