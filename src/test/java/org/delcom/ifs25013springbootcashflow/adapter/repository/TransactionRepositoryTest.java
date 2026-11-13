package org.delcom.ifs25013springbootcashflow.adapter.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveAssignsIncrementingIdsAndKeepsFields() {
        Transaction first = repository.save(1, "Gaji", 100, TransactionType.INCOME, "a.png");
        Transaction second = repository.save(2, "Makan", 20, TransactionType.EXPENSE, null);

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(1, first.getUserId());
        assertEquals("a.png", first.getProofPath());
        assertNotNull(first.getCreatedAt());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByUserIdOnlyReturnsOwnTransactions() {
        repository.save(1, "A", 1, TransactionType.INCOME, null);
        repository.save(2, "B", 2, TransactionType.INCOME, null);
        repository.save(1, "C", 3, TransactionType.EXPENSE, null);

        List<Transaction> mine = repository.findByUserId(1);

        assertEquals(2, mine.size());
        assertTrue(mine.stream().allMatch(transaction -> transaction.getUserId() == 1));
        assertTrue(repository.findByUserId(99).isEmpty());
    }

    @Test
    void genericOperationsFindDeleteAndUpdate() {
        Transaction saved = repository.save(1, "A", 10, TransactionType.INCOME, null);

        assertTrue(repository.findById(saved.getId()).isPresent());
        assertFalse(repository.findById(99).isPresent());

        saved.changeAmount(50);
        repository.update(saved);
        assertEquals(50L, repository.findById(saved.getId()).orElseThrow().getAmount());

        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.save(1, "A", 10, TransactionType.INCOME, null);

        repository.findAll().clear();

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void idsAreNotReusedAfterDelete() {
        Transaction first = repository.save(1, "A", 10, TransactionType.INCOME, null);
        repository.deleteById(first.getId());

        Transaction second = repository.save(1, "B", 10, TransactionType.INCOME, null);

        assertEquals(2, second.getId());
    }
}
