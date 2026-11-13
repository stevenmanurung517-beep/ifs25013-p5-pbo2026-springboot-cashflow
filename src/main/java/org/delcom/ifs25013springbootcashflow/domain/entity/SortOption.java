package org.delcom.ifs25013springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi. Setiap opsi membawa comparator-nya sendiri
 * sehingga aturan pengurutan terpusat di domain, bukan tersebar di view.
 */
public enum SortOption {

    /** Urutkan dari jumlah terkecil ke terbesar. */
    AMOUNT_ASC(Comparator.comparingLong(Transaction::getAmount)),

    /** Urutkan dari jumlah terbesar ke terkecil. */
    AMOUNT_DESC(Comparator.comparingLong(Transaction::getAmount).reversed()),

    /** Tampilkan pemasukan terlebih dahulu. */
    INCOME_FIRST(Comparator.comparing(Transaction::getType)),

    /** Tampilkan pengeluaran terlebih dahulu. */
    EXPENSE_FIRST(Comparator.comparing(Transaction::getType).reversed());

    /** Comparator yang digunakan untuk mengurutkan daftar transaksi. */
    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    /** Mengembalikan comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
