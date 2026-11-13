package org.delcom.ifs25013springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 12, 9, 30);

    @Test
    void exposesAllFields() {
        Transaction transaction = new Transaction(1, 7, "Gaji", 5_000_000L,
                TransactionType.INCOME, "bukti.png", NOW);

        assertEquals(1, transaction.getId());
        assertEquals(7, transaction.getUserId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(5_000_000L, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals("bukti.png", transaction.getProofPath());
        assertEquals(NOW, transaction.getCreatedAt());
    }

    @Test
    void signedAmountIsPositiveForIncomeAndNegativeForExpense() {
        Transaction income = new Transaction(1, 1, "Gaji", 100, TransactionType.INCOME, null, NOW);
        Transaction expense = new Transaction(2, 1, "Makan", 40, TransactionType.EXPENSE, null, NOW);

        assertTrue(income.isIncome());
        assertEquals(100L, income.signedAmount());
        assertFalse(expense.isIncome());
        assertEquals(-40L, expense.signedAmount());
    }

    @Test
    void changeMethodsUpdateMutableFields() {
        Transaction transaction = new Transaction(1, 1, "Lama", 10, TransactionType.EXPENSE, "a.png", NOW);

        transaction.changeDescription("Baru");
        transaction.changeAmount(99);
        transaction.changeProofPath(null);

        assertEquals("Baru", transaction.getDescription());
        assertEquals(99L, transaction.getAmount());
        assertNull(transaction.getProofPath());
    }
}
