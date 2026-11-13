package org.delcom.ifs25013springbootcashflow.domain.entity;

/**
 * Peran pengguna aplikasi.
 */
public enum Role {

    /** Administrator: melihat daftar pengguna dan transaksi pengguna lain (read-only). */
    ADMIN,

    /** Pengguna biasa: hanya mengelola transaksi miliknya sendiri. */
    USER
}
