package org.delcom.ifs25013springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private static Transaction tx(int id, long amount, TransactionType type) {
        return new Transaction(id, 1, "t" + id, amount, type, null, LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    private static List<Integer> ids(SortOption option, Transaction... items) {
        List<Transaction> list = new ArrayList<>(List.of(items));
        list.sort(option.comparator());
        return list.stream().map(Transaction::getId).toList();
    }

    @Test
    void amountAscendingAndDescending() {
        Transaction a = tx(1, 300, TransactionType.INCOME);
        Transaction b = tx(2, 100, TransactionType.EXPENSE);
        Transaction c = tx(3, 200, TransactionType.INCOME);

        assertEquals(List.of(2, 3, 1), ids(SortOption.AMOUNT_ASC, a, b, c));
        assertEquals(List.of(1, 3, 2), ids(SortOption.AMOUNT_DESC, a, b, c));
    }

    @Test
    void incomeFirstAndExpenseFirst() {
        Transaction expense = tx(1, 10, TransactionType.EXPENSE);
        Transaction income = tx(2, 10, TransactionType.INCOME);

        assertEquals(List.of(2, 1), ids(SortOption.INCOME_FIRST, expense, income));
        assertEquals(List.of(1, 2), ids(SortOption.EXPENSE_FIRST, expense, income));
    }

    @Test
    void hasFourOptions() {
        assertEquals(4, SortOption.values().length);
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
    }
}
