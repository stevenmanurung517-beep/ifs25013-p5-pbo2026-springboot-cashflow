package org.delcom.ifs25013springbootcashflow.usecase;

import org.delcom.ifs25013springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25013springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int USER = 1;
    private static final int OTHER = 2;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addIncomeAndExpenseTrimDescriptionAndKeepProof() throws Exception {
        Transaction income = useCase.addIncome(USER, "  Gaji  ", " 5000000 ", "bukti.png");
        Transaction expense = useCase.addExpense(USER, "Makan", "25000", null);

        assertEquals("Gaji", income.getDescription());
        assertEquals(5_000_000L, income.getAmount());
        assertTrue(income.isIncome());
        assertEquals("bukti.png", income.getProofPath());
        assertFalse(expense.isIncome());
        assertEquals(2, useCase.getTransactions(USER).size());
    }

    @Test
    void addRejectsInvalidInput() {
        assertEquals("Keterangan transaksi tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.addIncome(USER, " ", "100", null)).getMessage());
        assertEquals("Keterangan transaksi tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.addIncome(USER, null, "100", null)).getMessage());
        assertEquals("Jumlah tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "A", "", null)).getMessage());
        assertEquals("Jumlah tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "A", null, null)).getMessage());
        assertEquals("Jumlah harus berupa angka bulat",
                assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "A", "abc", null)).getMessage());
        assertEquals("Jumlah harus berupa angka bulat",
                assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "A", "12.5", null)).getMessage());
        assertEquals("Jumlah harus lebih besar dari 0",
                assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "A", "0", null)).getMessage());
        assertEquals("Jumlah harus lebih besar dari 0",
                assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "A", "-5", null)).getMessage());
        assertTrue(useCase.getTransactions(USER).isEmpty());
    }

    @Test
    void updateChangesOwnTransaction() throws Exception {
        Transaction saved = useCase.addIncome(USER, "Gaji", "100", null);

        Transaction updated = useCase.updateTransaction(USER, saved.getId(), " Gaji Baru ", "250");

        assertEquals("Gaji Baru", updated.getDescription());
        assertEquals(250L, updated.getAmount());
        assertEquals(250L, useCase.getTransactions(USER).get(0).getAmount());
    }

    @Test
    void updateRejectsInvalidIdMissingForeignAndBadInput() throws Exception {
        Transaction saved = useCase.addIncome(USER, "Gaji", "100", null);

        assertEquals("ID tidak valid",
                assertThrows(ValidationException.class, () -> useCase.updateTransaction(USER, 0, "A", "1")).getMessage());
        EntityNotFoundException notFound = assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransaction(USER, 99, "A", "1"));
        assertEquals(99, notFound.getId());
        assertEquals("Transaksi ini bukan milik Anda",
                assertThrows(ValidationException.class,
                        () -> useCase.updateTransaction(OTHER, saved.getId(), "A", "1")).getMessage());
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(USER, saved.getId(), " ", "1"));
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(USER, saved.getId(), "A", "x"));
        assertEquals("Gaji", useCase.getTransactions(USER).get(0).getDescription());
    }

    @Test
    void removeDeletesOnlyOwnTransaction() throws Exception {
        Transaction mine = useCase.addIncome(USER, "A", "10", null);
        Transaction theirs = useCase.addIncome(OTHER, "B", "20", null);

        assertEquals("ID tidak valid",
                assertThrows(ValidationException.class, () -> useCase.removeTransaction(USER, -1)).getMessage());
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(USER, 99));
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(USER, theirs.getId()));

        useCase.removeTransaction(USER, mine.getId());

        assertTrue(useCase.getTransactions(USER).isEmpty());
        assertEquals(1, useCase.getTransactions(OTHER).size());
    }

    @Test
    void searchIsCaseInsensitiveScopedToUserAndHandlesNullKeyword() throws Exception {
        useCase.addIncome(USER, "Gaji Bulanan", "100", null);
        useCase.addExpense(USER, "Makan", "10", null);
        useCase.addExpense(OTHER, "Gaji orang lain", "10", null);

        assertEquals(1, useCase.searchTransactions(USER, "  GAJI ").size());
        assertEquals(0, useCase.searchTransactions(USER, "xyz").size());
        assertEquals(2, useCase.searchTransactions(USER, null).size());
        assertEquals(2, useCase.searchTransactions(USER, "").size());
    }

    @Test
    void sortUsesComparatorOfEachOption() throws Exception {
        useCase.addExpense(USER, "Kopi", "10", null);
        useCase.addIncome(USER, "Gaji", "500", null);
        useCase.addExpense(USER, "Buku", "200", null);

        assertEquals(List.of("Kopi", "Buku", "Gaji"), descriptions(SortOption.AMOUNT_ASC));
        assertEquals(List.of("Gaji", "Buku", "Kopi"), descriptions(SortOption.AMOUNT_DESC));
        assertEquals("Gaji", descriptions(SortOption.INCOME_FIRST).get(0));
        assertEquals("Gaji", descriptions(SortOption.EXPENSE_FIRST).get(2));
    }

    @Test
    void totalsAndBalanceAreComputedPerUser() throws Exception {
        useCase.addIncome(USER, "Gaji", "5000000", null);
        useCase.addExpense(USER, "Makan", "25000", null);
        useCase.addIncome(OTHER, "Lain", "999", null);

        assertEquals(5_000_000L, useCase.getTotalIncome(USER));
        assertEquals(25_000L, useCase.getTotalExpense(USER));
        assertEquals(4_975_000L, useCase.getBalance(USER));
        assertEquals(999L, useCase.getBalance(OTHER));
        assertEquals(0L, useCase.getBalance(3));
    }

    private List<String> descriptions(SortOption option) {
        return useCase.sortTransactions(USER, option).stream().map(Transaction::getDescription).toList();
    }
}
