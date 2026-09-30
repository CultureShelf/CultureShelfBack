package com.example.cultureshelf.catalog.service;

import java.util.Map;
import java.util.UUID;

import com.example.cultureshelf.catalog.model.Series;
import com.example.cultureshelf.catalog.model.Variant;
import com.example.cultureshelf.catalog.model.VariantImage;
import com.example.cultureshelf.catalog.model.Volume;
import com.example.cultureshelf.catalog.web.CatalogDtos.ImageDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesDetailDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VariantDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VolumeDto;
import com.example.cultureshelf.collection.CollectionItem;
import com.example.cultureshelf.collection.CollectionItemDto;

/** Conversion entites -> DTO. A appeler dans une transaction (associations lazy). */
public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static SeriesDetailDto toDetail(Series series, Map<UUID, CollectionItem> itemsByVariant) {
        return new SeriesDetailDto(
                series.getId(), series.getMediaKind(), series.getSource(), series.getExternalId(),
                series.getTitle(), series.getOriginalTitle(), series.getAuthors(), series.getDescription(),
                series.getCoverUrl(), series.getYear(), series.getStatus(),
                series.getVolumes().stream().map(v -> toVolume(v, itemsByVariant)).toList());
    }

    public static VolumeDto toVolume(Volume volume, Map<UUID, CollectionItem> itemsByVariant) {
        return new VolumeDto(volume.getId(), volume.getNumber(), volume.getTitle(), volume.getCoverUrl(),
                volume.getVariants().stream().map(v -> toVariant(v, itemsByVariant.get(v.getId()))).toList());
    }

    public static VariantDto toVariant(Variant variant, CollectionItem item) {
        return new VariantDto(
                variant.getId(), variant.getType(), variant.getName(), variant.getPublisher(), variant.getIsbn(),
                variant.getReleaseDate(), variant.getPrice(), variant.getPrintRun(), variant.getDescription(),
                variant.getExtras(),
                variant.getImages().stream().map(CatalogMapper::toImage).toList(),
                CollectionItemDto.of(item));
    }

    public static ImageDto toImage(VariantImage image) {
        return new ImageDto(image.getId(), image.getUrl(), image.getKind(), image.getCaption());
    }
}
