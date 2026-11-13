package org.delcom.ifs25013springbootcashflow.domain.repository;

import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.TransactionType;

import java.util.List;

/**
 * Port spesifik Transaction yang memperluas generic {@link IRepository}.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    /**
     * Menyimpan transaksi baru milik seorang pengguna.
     *
     * @param proofPath path relatif bukti transaksi, boleh null
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath);

    /** Mengambil semua transaksi milik satu pengguna, terurut menurut ID. */
    List<Transaction> findByUserId(int userId);
}
