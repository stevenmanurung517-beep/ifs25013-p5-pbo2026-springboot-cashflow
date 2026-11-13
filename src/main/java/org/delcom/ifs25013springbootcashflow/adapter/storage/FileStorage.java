package org.delcom.ifs25013springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Menyimpan berkas unggahan (foto profil dan bukti transaksi) ke folder UPLOAD_DIR.
 * Nama berkas diganti UUID + ekstensi asli; yang dikembalikan hanya nama relatifnya,
 * dan berkas dilayani lewat URL {@code /uploads/**}.
 */
public class FileStorage {

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /** Menyimpan isi berkas dan mengembalikan nama berkas relatif (UUID + ekstensi). */
    public String store(String originalFilename, InputStream content) throws IOException {
        Files.createDirectories(root);
        String name = UUID.randomUUID() + extensionOf(originalFilename);
        Files.copy(content, root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return name;
    }

    /** Menghapus berkas bila ada. Mengembalikan true jika berkas benar-benar terhapus. */
    public boolean delete(String name) throws IOException {
        return Files.deleteIfExists(root.resolve(name).normalize());
    }

    /** Folder tempat berkas disimpan. */
    public Path getRoot() {
        return root;
    }

    /** URI folder (berakhiran "/") untuk dipakai sebagai resource location Spring MVC. */
    public String getRootUri() {
        String uri = root.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String extension = filename.substring(dot + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return extension.isEmpty() ? "" : "." + extension;
    }
}
