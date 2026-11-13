package org.delcom.ifs25013springbootcashflow.adapter.presenter;

import org.delcom.ifs25013springbootcashflow.domain.entity.Role;
import org.delcom.ifs25013springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.TransactionType;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Presenter yang memformat data domain menjadi teks siap tampil.
 * Format uang dan pesan bukan tanggung jawab entity maupun use case.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<TransactionType, String> TYPE_LABELS = Map.of(
            TransactionType.INCOME, "Pemasukan",
            TransactionType.EXPENSE, "Pengeluaran"
    );

    private static final Map<SortOption, String> SORT_LABELS = Map.of(
            SortOption.AMOUNT_ASC, "Jumlah terkecil",
            SortOption.AMOUNT_DESC, "Jumlah terbesar",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu"
    );

    private static final Map<Role, String> ROLE_LABELS = Map.of(
            Role.ADMIN, "Admin",
            Role.USER, "Pengguna"
    );

    /** Memformat uang, contoh: 1000000 menjadi "Rp 1,000,000.00". */
    public String formatMoney(long amount) {
        return "Rp " + String.format(Locale.US, "%,d.00", amount);
    }

    /** Memformat jumlah milik satu transaksi. */
    public String formatAmount(Transaction transaction) {
        return formatMoney(transaction.getAmount());
    }

    /** Label tipe transaksi: "Pemasukan" atau "Pengeluaran". */
    public String formatType(Transaction transaction) {
        return TYPE_LABELS.get(transaction.getType());
    }

    /** Label opsi pengurutan untuk ditampilkan di UI. */
    public String formatSortOption(SortOption option) {
        return SORT_LABELS.get(option);
    }

    /** Label role: "Admin" atau "Pengguna". */
    public String formatRole(Role role) {
        return ROLE_LABELS.get(role);
    }

    /** Memformat waktu pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat satu transaksi: {@code [id] keterangan | Rp 1,000,000.00 | [Pemasukan]}. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatAmount(transaction),
                formatType(transaction)
        );
    }

    /** Memformat saldo: {@code Saldo: Rp 4,975,000.00}. */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String showListHeader() {
        return "Daftar Transaksi";
    }

    public String showSearchHeader(String keyword) {
        return "Hasil Pencarian: \"" + keyword + "\"";
    }

    public String showSortHeader(SortOption option) {
        return "Daftar Transaksi (Urut: " + formatSortOption(option) + ")";
    }

    public String showEmptyMessage() {
        return "Data transaksi belum tersedia!";
    }

    public String showNotFoundMessage() {
        return "Transaksi tidak ditemukan!";
    }

    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah " + formatType(transaction).toLowerCase(Locale.ROOT)
                + ": " + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }

    public String showProfileSaved() {
        return "Profil berhasil disimpan.";
    }

    public String showUploadFailed() {
        return "[!] Gagal menyimpan berkas unggahan.";
    }
}
