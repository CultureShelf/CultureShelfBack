package com.example.cultureshelf.catalog.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.cultureshelf.catalog.model.ImageKind;
import com.example.cultureshelf.catalog.model.Variant;
import com.example.cultureshelf.catalog.model.VariantImage;
import com.example.cultureshelf.catalog.model.Volume;
import com.example.cultureshelf.catalog.repo.VariantImageRepository;
import com.example.cultureshelf.catalog.repo.VariantRepository;
import com.example.cultureshelf.catalog.repo.VolumeRepository;
import com.example.cultureshelf.catalog.web.CatalogDtos.ImageUrlRequest;
import com.example.cultureshelf.catalog.web.CatalogDtos.VariantDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VariantRequest;
import com.example.cultureshelf.collection.CollectionItemRepository;
import com.example.cultureshelf.common.NotFoundException;
import com.example.cultureshelf.storage.FileStorageService;

/** Donnees collector : variantes d'un tome et leurs visuels. */
@Service
public class VariantService {

    private final VolumeRepository volumeRepository;
    private final VariantRepository variantRepository;
    private final VariantImageRepository imageRepository;
    private final CollectionItemRepository collectionRepository;
    private final FileStorageService storage;

    public VariantService(VolumeRepository volumeRepository, VariantRepository variantRepository,
            VariantImageRepository imageRepository, CollectionItemRepository collectionRepository,
            FileStorageService storage) {
        this.volumeRepository = volumeRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.collectionRepository = collectionRepository;
        this.storage = storage;
    }

    @Transactional
    public VariantDto create(UUID volumeId, VariantRequest request) {
        Volume volume = volumeRepository.findById(volumeId).orElseThrow(() -> new NotFoundException("Tome", volumeId));
        Variant variant = new Variant();
        apply(variant, request);
        volume.addVariant(variant);
        variantRepository.saveAndFlush(variant);
        return CatalogMapper.toVariant(variant, null);
    }

    @Transactional
    public VariantDto update(UUID id, VariantRequest request) {
        Variant variant = find(id);
        apply(variant, request);
        return toDto(variant);
    }

    @Transactional
    public void delete(UUID id) {
        Variant variant = find(id);
        collectionRepository.findByVariantId(id).ifPresent(collectionRepository::delete);
        variant.getImages().stream().map(VariantImage::getStoredFile).filter(f -> f != null).forEach(storage::delete);
        variant.getVolume().getVariants().remove(variant);
    }

    @Transactional
    public VariantDto addImage(UUID variantId, MultipartFile file, ImageKind kind, String caption) {
        Variant variant = find(variantId);
        String storedFile = storage.store(file);
        VariantImage image = new VariantImage();
        image.setStoredFile(storedFile);
        image.setUrl(FileStorageService.publicUrl(storedFile));
        image.setKind(kind != null ? kind : ImageKind.FRONT);
        image.setCaption(caption);
        variant.addImage(image);
        imageRepository.saveAndFlush(image);
        return toDto(variant);
    }

    @Transactional
    public VariantDto addImageUrl(UUID variantId, ImageUrlRequest request) {
        if (!request.url().startsWith("https://") && !request.url().startsWith("http://")) {
            throw new IllegalArgumentException("L'URL de l'image doit commencer par http:// ou https://");
        }
        Variant variant = find(variantId);
        VariantImage image = new VariantImage();
        image.setUrl(request.url());
        image.setKind(request.kind() != null ? request.kind() : ImageKind.FRONT);
        image.setCaption(request.caption());
        variant.addImage(image);
        imageRepository.saveAndFlush(image);
        return toDto(variant);
    }

    @Transactional
    public VariantDto deleteImage(UUID imageId) {
        VariantImage image = imageRepository.findById(imageId).orElseThrow(() -> new NotFoundException("Image", imageId));
        Variant variant = image.getVariant();
        variant.getImages().remove(image);
        for (int i = 0; i < variant.getImages().size(); i++) {
            variant.getImages().get(i).setSortOrder(i);
        }
        if (image.getStoredFile() != null) {
            storage.delete(image.getStoredFile());
        }
        return toDto(variant);
    }

    private VariantDto toDto(Variant variant) {
        return CatalogMapper.toVariant(variant, collectionRepository.findByVariantId(variant.getId()).orElse(null));
    }

    private Variant find(UUID id) {
        return variantRepository.findById(id).orElseThrow(() -> new NotFoundException("Variante", id));
    }

    private static void apply(Variant variant, VariantRequest request) {
        variant.setType(request.type());
        variant.setName(request.name());
        variant.setPublisher(request.publisher());
        variant.setIsbn(request.isbn());
        variant.setReleaseDate(request.releaseDate());
        variant.setPrice(request.price());
        variant.setPrintRun(request.printRun());
        variant.setDescription(request.description());
        variant.setExtras(request.extras());
    }
}
