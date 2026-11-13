package org.delcom.ifs25013springbootcashflow.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DomainExceptionTest {

    @Test
    void domainExceptionKeepsMessageAndCause() {
        Throwable cause = new RuntimeException("akar");

        assertEquals("pesan", new DomainException("pesan").getMessage());
        DomainException withCause = new DomainException("pesan", cause);
        assertEquals("pesan", withCause.getMessage());
        assertSame(cause, withCause.getCause());
    }

    @Test
    void validationExceptionIsDomainException() {
        ValidationException exception = new ValidationException("tidak valid");

        assertInstanceOf(DomainException.class, exception);
        assertEquals("tidak valid", exception.getMessage());
    }

    @Test
    void entityNotFoundCarriesNameAndId() {
        EntityNotFoundException exception = new EntityNotFoundException("Transaksi", 42);

        assertInstanceOf(DomainException.class, exception);
        assertEquals(42, exception.getId());
        assertEquals("Transaksi dengan ID 42 tidak ditemukan.", exception.getMessage());
    }
}
