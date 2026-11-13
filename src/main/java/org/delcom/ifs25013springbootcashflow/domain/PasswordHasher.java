package org.delcom.ifs25013springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash dan verifikasi password dengan SHA-256 (hasil dalam heksadesimal).
 * Hanya memakai pustaka standar JDK, sehingga aman berada di layer domain.
 */
public class PasswordHasher {

    private final String algorithm;

    /** Memakai SHA-256 sesuai ketentuan praktikum. */
    public PasswordHasher() {
        this("SHA-256");
    }

    /** Memakai algoritma lain (dipakai pengujian untuk jalur algoritma tidak dikenal). */
    public PasswordHasher(String algorithm) {
        this.algorithm = algorithm;
    }

    /** Menghasilkan hash heksadesimal dari password. */
    public String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritma hash tidak tersedia: " + algorithm, ex);
        }
    }

    /** Mengembalikan true jika password mentah cocok dengan hash yang tersimpan. */
    public boolean matches(String rawPassword, String expectedHash) {
        return hash(rawPassword).equals(expectedHash);
    }
}
