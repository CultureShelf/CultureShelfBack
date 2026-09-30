package com.example.cultureshelf.catalog.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.cultureshelf.catalog.model.ImageKind;
import com.example.cultureshelf.catalog.service.VariantService;
import com.example.cultureshelf.catalog.web.CatalogDtos.ImageUrlRequest;
import com.example.cultureshelf.catalog.web.CatalogDtos.VariantDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VariantRequest;

@RestController
@RequestMapping("/api/v1")
public class VariantController {

    private final VariantService service;

    public VariantController(VariantService service) {
        this.service = service;
    }

    @PostMapping("/volumes/{volumeId}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    public VariantDto create(@PathVariable UUID volumeId, @Validated @RequestBody VariantRequest request) {
        return service.create(volumeId, request);
    }

    @PutMapping("/variants/{id}")
    public VariantDto update(@PathVariable UUID id, @Validated @RequestBody VariantRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/variants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    /** Upload d'un visuel (multipart : file, kind, caption). */
    @PostMapping(path = "/variants/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VariantDto uploadImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) ImageKind kind, @RequestParam(required = false) String caption) {
        return service.addImage(id, file, kind, caption);
    }

    /** Ajout d'un visuel par URL externe. */
    @PostMapping(path = "/variants/{id}/images", consumes = MediaType.APPLICATION_JSON_VALUE)
    public VariantDto addImageUrl(@PathVariable UUID id, @Validated @RequestBody ImageUrlRequest request) {
        return service.addImageUrl(id, request);
    }

    @DeleteMapping("/images/{id}")
    public VariantDto deleteImage(@PathVariable UUID id) {
        return service.deleteImage(id);
    }
}
