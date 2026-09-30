package com.example.cultureshelf.collection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cultureshelf.catalog.model.Variant;
import com.example.cultureshelf.catalog.model.VariantType;
import com.example.cultureshelf.catalog.model.Volume;
import com.example.cultureshelf.catalog.repo.VariantRepository;
import com.example.cultureshelf.common.NotFoundException;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Service
public class CollectionService {

    public record CollectionItemRequest(
            @NotNull CollectionStatus status,
            ItemCondition condition,
            @Min(1) Integer quantity,
            LocalDate acquiredAt,
            BigDecimal pricePaid,
            @Size(max = 2000) String notes) {
    }

    /** Vue a plat d'un exemplaire, avec le contexte catalogue necessaire a l'affichage. */
    public record CollectionEntryDto(
            CollectionItemDto item,
            UUID seriesId,
            String seriesTitle,
            UUID volumeId,
            String volumeNumber,
            UUID variantId,
            String variantName,
            VariantType variantType,
            String coverUrl) {
    }

    private final CollectionItemRepository repository;
    private final VariantRepository variantRepository;

    public CollectionService(CollectionItemRepository repository, VariantRepository variantRepository) {
        this.repository = repository;
        this.variantRepository = variantRepository;
    }

    @Transactional(readOnly = true)
    public List<CollectionEntryDto> list(CollectionStatus status) {
        return repository.findAllWithCatalog(status).stream().map(CollectionService::toEntry).toList();
    }

    /** Ajoute la variante a la collection, ou met a jour l'exemplaire existant. */
    @Transactional
    public CollectionItemDto upsert(UUID variantId, CollectionItemRequest request) {
        CollectionItem item = repository.findByVariantId(variantId).orElseGet(() -> {
            Variant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new NotFoundException("Variante", variantId));
            CollectionItem created = new CollectionItem();
            created.setVariant(variant);
            return created;
        });
        item.setStatus(request.status());
        item.setCondition(request.condition());
        item.setQuantity(request.quantity() != null ? request.quantity() : 1);
        item.setAcquiredAt(request.acquiredAt());
        item.setPricePaid(request.pricePaid());
        item.setNotes(request.notes());
        return CollectionItemDto.of(repository.save(item));
    }

    @Transactional
    public void remove(UUID variantId) {
        repository.findByVariantId(variantId).ifPresent(repository::delete);
    }

    private static CollectionEntryDto toEntry(CollectionItem item) {
        Variant variant = item.getVariant();
        Volume volume = variant.getVolume();
        String cover = variant.getImages().isEmpty() ? volume.getCoverUrl() : variant.getImages().getFirst().getUrl();
        return new CollectionEntryDto(CollectionItemDto.of(item),
                volume.getSeries().getId(), volume.getSeries().getTitle(),
                volume.getId(), volume.getNumber(),
                variant.getId(), variant.getName(), variant.getType(), cover);
    }
}
