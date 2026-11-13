package org.delcom.ifs25013springbootcashflow.adapter.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.User;
import org.delcom.ifs25013springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Implementasi JDBC (PostgreSQL) untuk {@link IUserRepository}.
 */
public class JdbcUserRepository extends JdbcRepository<User> implements IUserRepository {

    public JdbcUserRepository(DataSource dataSource) {
        super(dataSource, "users");
    }

    @Override
    protected User mapRow(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("full_name"),
                Role.valueOf(resultSet.getString("role")),
                resultSet.getString("photo_path")
        );
    }

    @Override
    public User save(User entity) {
        int id = insert(
                "INSERT INTO users (username, password_hash, full_name, role, photo_path)"
                        + " VALUES (?, ?, ?, ?, ?)",
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getRole().name(),
                entity.getPhotoPath()
        );
        return new User(id, entity.getUsername(), entity.getPasswordHash(), entity.getFullName(),
                entity.getRole(), entity.getPhotoPath());
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(0, username, passwordHash, fullName, role, null));
    }

    @Override
    public void update(User entity) {
        update("UPDATE users SET full_name = ?, password_hash = ?, photo_path = ? WHERE id = ?",
                entity.getFullName(), entity.getPasswordHash(), entity.getPhotoPath(), entity.getId());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return query("SELECT * FROM users WHERE username = ?", username).stream().findFirst();
    }
}
