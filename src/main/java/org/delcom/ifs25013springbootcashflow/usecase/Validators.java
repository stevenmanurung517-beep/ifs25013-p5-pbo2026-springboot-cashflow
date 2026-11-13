package org.delcom.ifs25013springbootcashflow.usecase;

import org.delcom.ifs25013springbootcashflow.domain.exception.ValidationException;

/**
 * Pemeriksaan input sederhana yang dipakai bersama oleh beberapa use case.
 */
final class Validators {

    private Validators() {
    }

    /** Memastikan teks tidak null/kosong, lalu mengembalikannya dalam keadaan ter-trim. */
    static String requireText(String value, String message) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.trim();
    }
}
