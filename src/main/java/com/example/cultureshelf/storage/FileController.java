package com.example.cultureshelf.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cultureshelf.common.NotFoundException;

@RestController
@RequestMapping(FileStorageService.PUBLIC_PATH)
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("{name}")
    public ResponseEntity<Resource> get(@PathVariable String name) throws IOException {
        Path path = storage.resolve(name).orElseThrow(() -> new NotFoundException("Fichier", name));
        String contentType = Files.probeContentType(path);
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                // Les noms de fichiers sont des UUID : le contenu ne change jamais.
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(new FileSystemResource(path));
    }
}
