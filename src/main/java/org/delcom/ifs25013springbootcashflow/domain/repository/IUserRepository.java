package org.delcom.ifs25013springbootcashflow.domain.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;

import java.util.Optional;

/**
 * Port spesifik User. Daftar pengguna lewat {@code findAll()}, ubah profil/foto lewat {@code update(User)}.
 */
public interface IUserRepository extends IRepository<User, Integer> {

    /**
     * Menyimpan pengguna baru.
     *
     * @return pengguna yang tersimpan (lengkap dengan ID)
     */
    User save(String username, String passwordHash, String fullName, Role role);

    /** Mencari pengguna berdasarkan username (persis sama). */
    Optional<User> findByUsername(String username);
}
