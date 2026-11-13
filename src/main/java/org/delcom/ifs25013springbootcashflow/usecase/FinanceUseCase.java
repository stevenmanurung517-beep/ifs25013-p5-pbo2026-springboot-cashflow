package org.delcom.ifs25013springbootcashflow.usecase;

import org.delcom.ifs25013springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25013springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25013springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25013springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25013springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Objects;

/**
 * Use case logika bisnis Cashflow. Semua operasi dibatasi per pengguna (userId).
 * Tidak melakukan I/O UI; kegagalan dilaporkan lewat checked exception domain.
 */
public class FinanceUseCase {

    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Semua transaksi milik pengguna. */
    public List<Transaction> getTransactions(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Menambah pemasukan setelah validasi keterangan dan jumlah. */
    public Transaction addIncome(int userId, String description, String amount, String proofPath)
            throws ValidationException {
        return add(userId, description, amount, proofPath, TransactionType.INCOME);
    }

    /** Menambah pengeluaran setelah validasi keterangan dan jumlah. */
    public Transaction addExpense(int userId, String description, String amount, String proofPath)
            throws ValidationException {
        return add(userId, description, amount, proofPath, TransactionType.EXPENSE);
    }

    /** Mengubah keterangan dan jumlah transaksi milik sendiri. */
    public Transaction updateTransaction(int userId, int id, String description, String amount)
            throws ValidationException, EntityNotFoundException {
        Transaction transaction = findOwned(userId, id);
        String validDescription = validateDescription(description);
        long validAmount = parseAmount(amount);
        transaction.changeDescription(validDescription);
        transaction.changeAmount(validAmount);
        transactionRepository.update(transaction);
        return transaction;
    }

    /**
     * Menghapus transaksi milik sendiri.
     *
     * @throws ValidationException     jika ID tidak valid atau transaksi milik orang lain
     * @throws EntityNotFoundException jika transaksi dengan ID tersebut tidak ada
     */
    public void removeTransaction(int userId, int id) throws ValidationException, EntityNotFoundException {
        Transaction transaction = findOwned(userId, id);
        transactionRepository.deleteById(transaction.getId());
    }

    /** Mencari transaksi milik pengguna yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        String lowerKeyword = Objects.requireNonNullElse(keyword, "").trim().toLowerCase();
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi milik pengguna sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return transactionRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total pemasukan pengguna. */
    public long getTotalIncome(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(Transaction::isIncome)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Total pengeluaran pengguna. */
    public long getTotalExpense(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> !transaction.isIncome())
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Saldo = total pemasukan − total pengeluaran. */
    public long getBalance(int userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    private Transaction add(int userId, String description, String amount, String proofPath,
                            TransactionType type) throws ValidationException {
        String validDescription = validateDescription(description);
        long validAmount = parseAmount(amount);
        return transactionRepository.save(userId, validDescription, validAmount, type, proofPath);
    }

    private Transaction findOwned(int userId, int id) throws ValidationException, EntityNotFoundException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid");
        }
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi ini bukan milik Anda");
        }
        return transaction;
    }

    private String validateDescription(String description) throws ValidationException {
        return Validators.requireText(description, "Keterangan transaksi tidak boleh kosong");
    }

    private long parseAmount(String amount) throws ValidationException {
        String text = Validators.requireText(amount, "Jumlah tidak boleh kosong");
        long parsed;
        try {
            parsed = Long.parseLong(text);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka bulat");
        }
        if (parsed <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        return parsed;
    }
}
