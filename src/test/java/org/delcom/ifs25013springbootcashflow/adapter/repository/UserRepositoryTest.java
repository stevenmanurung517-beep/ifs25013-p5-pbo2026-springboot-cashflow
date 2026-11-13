package org.delcom.ifs25013springbootcashflow.adapter.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void saveAssignsIdsAndFindByUsernameIsExact() {
        User first = repository.save("steven", "h1", "Steven", Role.USER);
        User second = repository.save("admin", "h2", "Administrator", Role.ADMIN);

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertNull(first.getPhotoPath());
        assertEquals("admin", repository.findByUsername("admin").orElseThrow().getUsername());
        assertFalse(repository.findByUsername("ADMIN").isPresent());
        assertFalse(repository.findByUsername("tidak-ada").isPresent());
    }

    @Test
    void updateAndDeleteWork() {
        User user = repository.save("steven", "h1", "Steven", Role.USER);

        user.changeFullName("Steven M");
        repository.update(user);

        assertEquals("Steven M", repository.findById(user.getId()).orElseThrow().getFullName());
        assertTrue(repository.deleteById(user.getId()));
        assertTrue(repository.findAll().isEmpty());
    }
}
