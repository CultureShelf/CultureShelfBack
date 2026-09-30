package com.example.cultureshelf.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.cultureshelf.config.CultureShelfProperties;

/** Stockage des visuels uploades sur le disque local. */
@Service
public class FileStorageService {

    public static final String PUBLIC_PATH = "/api/v1/files/";

    private static final Pattern SAFE_NAME = Pattern.compile("[a-f0-9-]{36}\\.(jpg|png|webp|gif)");
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final Path root;

    public FileStorageService(CultureShelfProperties properties) {
        this.root = Path.of(properties.storage().path()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de creer le dossier de stockage " + root, e);
        }
    }

    /** @return le nom du fichier stocke */
    public String store(MultipartFile file) {
        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Format d'image non supporte : " + file.getContentType()
                    + " (jpeg, png, webp ou gif)");
        }
        String name = UUID.randomUUID() + "." + extension;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return name;
    }

    public Optional<Path> resolve(String name) {
        if (name == null || !SAFE_NAME.matcher(name).matches()) {
            return Optional.empty();
        }
        Path path = root.resolve(name);
        return Files.isRegularFile(path) ? Optional.of(path) : Optional.empty();
    }

    public void delete(String name) {
        resolve(name).ifPresent(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static String publicUrl(String name) {
        return PUBLIC_PATH + name;
    }
}
