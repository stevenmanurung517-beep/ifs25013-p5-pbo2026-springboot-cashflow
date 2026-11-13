package org.delcom.ifs25013springbootcashflow.adapter.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void storeWritesFileWithUuidNameAndOriginalExtension() throws Exception {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());

        String name = storage.store("Foto Saya.PNG", new ByteArrayInputStream("isi".getBytes(StandardCharsets.UTF_8)));

        assertTrue(name.endsWith(".png"));
        assertEquals(36 + 4, name.length());
        assertEquals("isi", Files.readString(storage.getRoot().resolve(name)));
    }

    @Test
    void deleteRemovesExistingFileOnly() throws Exception {
        FileStorage storage = new FileStorage(tempDir.toString());
        String name = storage.store("a.jpg", new ByteArrayInputStream(new byte[]{1}));

        assertTrue(storage.delete(name));
        assertFalse(storage.delete(name));
    }

    @Test
    void rootUriAlwaysEndsWithSlash() throws Exception {
        FileStorage storage = new FileStorage(tempDir.resolve("belum-ada").toString());
        assertTrue(storage.getRootUri().endsWith("/"));

        storage.store("a.png", new ByteArrayInputStream(new byte[]{1}));
        assertTrue(storage.getRootUri().endsWith("/"));
        assertEquals(tempDir.resolve("belum-ada").toAbsolutePath().normalize(), storage.getRoot());
    }

    @Test
    void extensionOfHandlesOddNames() {
        assertEquals("", FileStorage.extensionOf(null));
        assertEquals("", FileStorage.extensionOf("tanpa-ekstensi"));
        assertEquals("", FileStorage.extensionOf("titik-akhir."));
        assertEquals("", FileStorage.extensionOf("aneh.!!!"));
        assertEquals(".jpeg", FileStorage.extensionOf("a.b.JPEG"));
    }
}
