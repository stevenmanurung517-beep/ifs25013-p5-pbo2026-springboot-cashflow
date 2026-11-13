package org.delcom.ifs25013springbootcashflow.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashesWithSha256Hex() {
        PasswordHasher hasher = new PasswordHasher();

        // SHA-256("abc") yang sudah dikenal
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hasher.hash("abc"));
        assertEquals(64, hasher.hash("password").length());
    }

    @Test
    void matchesComparesRawPasswordWithHash() {
        PasswordHasher hasher = new PasswordHasher();
        String hash = hasher.hash("rahasia");

        assertTrue(hasher.matches("rahasia", hash));
        assertFalse(hasher.matches("salah", hash));
    }

    @Test
    void unknownAlgorithmIsReportedAsIllegalState() {
        PasswordHasher hasher = new PasswordHasher("ALGORITMA-TIDAK-ADA");

        assertThrows(IllegalStateException.class, () -> hasher.hash("abc"));
    }
}
