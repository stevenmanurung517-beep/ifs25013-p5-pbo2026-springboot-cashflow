package org.delcom.ifs25013springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entity inti yang merepresentasikan satu transaksi keuangan milik seorang pengguna.
 * Berada di layer domain, bebas dari urusan tampilan maupun penyimpanan.
 */
public class Transaction {

    /** ID unik transaksi, tidak boleh diubah setelah dibuat. */
    private final int id;

    /** ID pemilik transaksi. */
    private final int userId;

    /** Keterangan transaksi. */
    private String description;

    /** Jumlah uang dalam rupiah (selalu positif); arah ditentukan oleh {@link #type}. */
    private long amount;

    /** Jenis transaksi: pemasukan atau pengeluaran. */
    private final TransactionType type;

    /** Path relatif berkas bukti transaksi (opsional, boleh null). */
    private String proofPath;

    /** Waktu transaksi dicatat. */
    private final LocalDateTime createdAt;

    public Transaction(int id, int userId, String description, long amount,
                       TransactionType type, String proofPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Mengembalikan true jika transaksi ini adalah pemasukan. */
    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    /** Jumlah bertanda: positif untuk pemasukan, negatif untuk pengeluaran. */
    public long signedAmount() {
        return isIncome() ? amount : -amount;
    }

    /** Mengubah keterangan transaksi. */
    public void changeDescription(String description) {
        this.description = description;
    }

    /** Mengubah jumlah transaksi. */
    public void changeAmount(long amount) {
        this.amount = amount;
    }

    /** Mengubah (atau menghapus, bila null) path bukti transaksi. */
    public void changeProofPath(String proofPath) {
        this.proofPath = proofPath;
    }
}
