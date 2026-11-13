package org.delcom.ifs25013springbootcashflow.domain.exception;

/**
 * Dilempar saat validasi aturan bisnis atau input domain gagal.
 */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}
