package org.delcom.ifs25013springbootcashflow.adapter.presenter;

import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income = new Transaction(1, 7, "Gaji", 1_000_000L,
            TransactionType.INCOME, null, LocalDateTime.of(2026, 11, 13, 8, 5));
    private final Transaction expense = new Transaction(2, 7, "Makan siang", 25_000L,
            TransactionType.EXPENSE, "b.png", LocalDateTime.of(2026, 1, 2, 23, 59));

    @Test
    void formatsMoneyWithThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000L));
        assertEquals("Rp 0.00", presenter.formatMoney(0L));
        assertEquals("Rp 999.00", presenter.formatMoney(999L));
        assertEquals("Rp 1,000,000.00", presenter.formatAmount(income));
    }

    @Test
    void formatsBalanceIncludingNegative() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000L));
        assertEquals("Saldo: Rp -900,000.00", presenter.formatBalance(-900_000L));
    }

    @Test
    void formatsTransactionLine() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[2] Makan siang | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense));
        assertEquals("Pemasukan", presenter.formatType(income));
        assertEquals("Pengeluaran", presenter.formatType(expense));
    }

    @Test
    void formatsTimeSortOptionsAndRoles() {
        assertEquals("13/11/2026 08:05", presenter.formatCreatedAt(income));
        assertEquals("Jumlah terkecil", presenter.formatSortOption(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.formatSortOption(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.formatSortOption(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.formatSortOption(SortOption.EXPENSE_FIRST));
        assertEquals("Admin", presenter.formatRole(Role.ADMIN));
        assertEquals("Pengguna", presenter.formatRole(Role.USER));
    }

    @Test
    void producesHeadersAndMessages() {
        assertEquals("Daftar Transaksi", presenter.showListHeader());
        assertEquals("Hasil Pencarian: \"gaji\"", presenter.showSearchHeader("gaji"));
        assertEquals("Daftar Transaksi (Urut: Jumlah terbesar)", presenter.showSortHeader(SortOption.AMOUNT_DESC));
        assertEquals("Data transaksi belum tersedia!", presenter.showEmptyMessage());
        assertEquals("Transaksi tidak ditemukan!", presenter.showNotFoundMessage());
        assertEquals("Berhasil menambah pemasukan: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 5.", presenter.showRemoveFailed(5));
        assertEquals("[!] Jumlah tidak boleh kosong", presenter.showValidationError("Jumlah tidak boleh kosong"));
        assertEquals("Profil berhasil disimpan.", presenter.showProfileSaved());
        assertEquals("[!] Gagal menyimpan berkas unggahan.", presenter.showUploadFailed());
    }
}
