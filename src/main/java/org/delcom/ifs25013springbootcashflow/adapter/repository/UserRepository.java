package org.delcom.ifs25013springbootcashflow.adapter.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

/**
 * Implementasi in-memory repository pengguna (dipakai pada unit test).
 * Runtime memakai {@link JdbcUserRepository}.
 */
public class UserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {

    private int idCounter = 0;

    public UserRepository() {
        super(User::getId);
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(++idCounter, username, passwordHash, fullName, role, null));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findBy(user -> user.getUsername().equals(username)).stream().findFirst();
    }
}
