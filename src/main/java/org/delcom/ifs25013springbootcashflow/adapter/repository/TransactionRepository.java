package org.delcom.ifs25013springbootcashflow.adapter.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25013springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementasi in-memory repository transaksi (dipakai pada unit test).
 * Runtime memakai {@link JdbcTransactionRepository}.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount,
                            TransactionType type, String proofPath) {
        return save(new Transaction(++idCounter, userId, description, amount, type, proofPath,
                LocalDateTime.now()));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(transaction -> transaction.getUserId() == userId);
    }
}
