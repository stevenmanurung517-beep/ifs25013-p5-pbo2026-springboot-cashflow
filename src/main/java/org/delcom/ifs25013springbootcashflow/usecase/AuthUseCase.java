package org.delcom.ifs25013springbootcashflow.usecase;

import org.delcom.ifs25013springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;

/**
 * Use case autentikasi: registrasi, login, dan seed admin awal.
 * Tidak mengenal Vaadin/HTTP; penyimpanan sesi adalah urusan layer framework.
 */
public class AuthUseCase {

    private final IUserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthUseCase(IUserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /** Mendaftarkan pengguna baru dengan role USER. */
    public User register(String username, String password, String fullName) throws ValidationException {
        String validUsername = Validators.requireText(username, "Username tidak boleh kosong");
        String validName = Validators.requireText(fullName, "Nama lengkap tidak boleh kosong");
        Validators.requireText(password, "Password tidak boleh kosong");
        if (userRepository.findByUsername(validUsername).isPresent()) {
            throw new ValidationException("Username sudah digunakan");
        }
        return userRepository.save(validUsername, passwordHasher.hash(password), validName, Role.USER);
    }

    /** Memverifikasi kredensial; mengembalikan pengguna bila cocok. */
    public User login(String username, String password) throws ValidationException {
        String validUsername = Validators.requireText(username, "Username tidak boleh kosong");
        Validators.requireText(password, "Password tidak boleh kosong");
        User user = userRepository.findByUsername(validUsername)
                .orElseThrow(() -> new ValidationException("Username atau password salah"));
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    /** Membuat akun ADMIN dari konfigurasi ROOT_* bila belum ada. */
    public void ensureRootAdmin(String username, String password) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(username, passwordHasher.hash(password), "Administrator", Role.ADMIN);
        }
    }
}
