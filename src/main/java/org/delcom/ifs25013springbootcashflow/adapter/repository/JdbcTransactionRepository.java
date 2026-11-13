package org.delcom.ifs25013springbootcashflow.adapter.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25013springbootcashflow.domain.repository.ITransactionRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementasi JDBC (PostgreSQL) untuk {@link ITransactionRepository}.
 */
public class JdbcTransactionRepository extends JdbcRepository<Transaction>
        implements ITransactionRepository {

    public JdbcTransactionRepository(DataSource dataSource) {
        super(dataSource, "transactions");
    }

    @Override
    protected Transaction mapRow(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getString("description"),
                resultSet.getLong("amount"),
                TransactionType.valueOf(resultSet.getString("type")),
                resultSet.getString("proof_path"),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }

    @Override
    public Transaction save(Transaction entity) {
        int id = insert(
                "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?)",
                entity.getUserId(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getType().name(),
                entity.getProofPath(),
                Timestamp.valueOf(entity.getCreatedAt())
        );
        return new Transaction(id, entity.getUserId(), entity.getDescription(), entity.getAmount(),
                entity.getType(), entity.getProofPath(), entity.getCreatedAt());
    }

    @Override
    public Transaction save(int userId, String description, long amount,
                            TransactionType type, String proofPath) {
        return save(new Transaction(0, userId, description, amount, type, proofPath,
                LocalDateTime.now()));
    }

    @Override
    public void update(Transaction entity) {
        update("UPDATE transactions SET description = ?, amount = ?, proof_path = ? WHERE id = ?",
                entity.getDescription(), entity.getAmount(), entity.getProofPath(), entity.getId());
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return query("SELECT * FROM transactions WHERE user_id = ? ORDER BY id", userId);
    }
}
