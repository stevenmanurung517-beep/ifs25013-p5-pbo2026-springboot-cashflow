package org.delcom.ifs25013springbootcashflow.adapter.repository;

import org.delcom.ifs25013springbootcashflow.domain.repository.IRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Base generic repository berbasis JDBC. Operasi umum (findAll, findById, deleteById, findBy)
 * ditulis sekali di sini; subclass hanya mendefinisikan nama tabel, pemetaan baris, save, dan update.
 * Semua SQLException dibungkus menjadi satu jenis exception runtime di satu tempat.
 *
 * @param <T> tipe entitas domain (kunci selalu Integer)
 */
public abstract class JdbcRepository<T> implements IRepository<T, Integer> {

    /** Pekerjaan yang membutuhkan koneksi dan boleh melempar SQLException. */
    @FunctionalInterface
    private interface SqlWork<R> {
        R apply(Connection connection) throws SQLException;
    }

    private final DataSource dataSource;
    private final String table;

    protected JdbcRepository(DataSource dataSource, String table) {
        this.dataSource = dataSource;
        this.table = table;
    }

    /** Memetakan baris hasil query menjadi entitas. */
    protected abstract T mapRow(ResultSet resultSet) throws SQLException;

    @Override
    public List<T> findAll() {
        return query("SELECT * FROM " + table + " ORDER BY id");
    }

    @Override
    public Optional<T> findById(Integer id) {
        return query("SELECT * FROM " + table + " WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public boolean deleteById(Integer id) {
        return update("DELETE FROM " + table + " WHERE id = ?", id) > 0;
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    /** Menjalankan SELECT dan memetakan setiap baris. */
    protected List<T> query(String sql, Object... params) {
        return withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(mapRow(resultSet));
                    }
                    return result;
                }
            }
        });
    }

    /** Menjalankan INSERT/UPDATE/DELETE; mengembalikan jumlah baris terpengaruh. */
    protected int update(String sql, Object... params) {
        return withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, params);
                return statement.executeUpdate();
            }
        });
    }

    /** Menjalankan INSERT dan mengembalikan ID yang dibuat database. */
    protected int insert(String sql, Object... params) {
        return withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(statement, params);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    return keys.getInt(1);
                }
            }
        });
    }

    private <R> R withConnection(SqlWork<R> work) {
        try (Connection connection = dataSource.getConnection()) {
            return work.apply(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Operasi database gagal: " + ex.getMessage(), ex);
        }
    }

    private static void bind(PreparedStatement statement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
