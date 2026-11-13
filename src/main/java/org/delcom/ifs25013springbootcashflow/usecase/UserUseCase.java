package org.delcom.ifs25013springbootcashflow.usecase;

import org.delcom.ifs25013springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

/**
 * Use case pengelolaan pengguna: daftar pengguna, ambil pengguna, ubah profil dan foto.
 */
public class UserUseCase {

    private final IUserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserUseCase(IUserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /** Daftar semua pengguna. */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Mengambil satu pengguna berdasarkan ID. */
    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /**
     * Mengubah nama lengkap dan, bila diisi, password.
     * Password kosong berarti password tidak diubah.
     */
    public User updateProfile(int id, String fullName, String newPassword)
            throws ValidationException, EntityNotFoundException {
        User user = getUser(id);
        String validName = Validators.requireText(fullName, "Nama lengkap tidak boleh kosong");
        user.changeFullName(validName);
        if (newPassword != null && !newPassword.isBlank()) {
            user.changePasswordHash(passwordHasher.hash(newPassword));
        }
        userRepository.update(user);
        return user;
    }

    /** Mengubah path foto profil. */
    public User updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        User user = getUser(id);
        user.changePhotoPath(photoPath);
        userRepository.update(user);
        return user;
    }
}
