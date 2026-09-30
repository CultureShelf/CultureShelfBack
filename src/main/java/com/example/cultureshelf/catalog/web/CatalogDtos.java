package com.example.cultureshelf.catalog.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.example.cultureshelf.catalog.model.ImageKind;
import com.example.cultureshelf.catalog.model.MediaKind;
import com.example.cultureshelf.catalog.model.VariantType;
import com.example.cultureshelf.collection.CollectionItemDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record ProviderDto(String source, String displayName) {
    }

    /** Resultat de recherche chez un fournisseur ; importedSeriesId est renseigne si la serie est deja importee. */
    public record SearchResultDto(
            String source,
            String externalId,
            MediaKind mediaKind,
            String title,
            String originalTitle,
            List<String> authors,
            String description,
            String coverUrl,
            Integer year,
            String status,
            UUID importedSeriesId) {
    }

    public record ImportRequest(@NotBlank String source, @NotBlank String externalId) {
    }

    public record SeriesSummaryDto(
            UUID id,
            MediaKind mediaKind,
            String title,
            String authors,
            String coverUrl,
            long volumeCount,
            long ownedVolumes,
            long ownedVariants,
            long preorderedVariants,
            long wishlistVariants) {
    }

    public record SeriesDetailDto(
            UUID id,
            MediaKind mediaKind,
            String source,
            String externalId,
            String title,
            String originalTitle,
            String authors,
            String description,
            String coverUrl,
            Integer year,
            String status,
            List<VolumeDto> volumes) {
    }

    public record SeriesRequest(
            @NotBlank @Size(max = 500) String title,
            MediaKind mediaKind,
            @Size(max = 500) String originalTitle,
            @Size(max = 500) String authors,
            @Size(max = 10000) String description,
            @Size(max = 1000) String coverUrl,
            Integer year,
            @Size(max = 40) String status) {
    }

    public record VolumeDto(UUID id, String number, String title, String coverUrl, List<VariantDto> variants) {
    }

    public record VolumeRequest(
            @NotBlank @Size(max = 40) String number,
            @Size(max = 500) String title,
            @Size(max = 1000) String coverUrl) {
    }

    public record VariantDto(
            UUID id,
            VariantType type,
            String name,
            String publisher,
            String isbn,
            LocalDate releaseDate,
            BigDecimal price,
            Integer printRun,
            String description,
            String extras,
            List<ImageDto> images,
            CollectionItemDto collection) {
    }

    public record VariantRequest(
            @NotNull VariantType type,
            @NotBlank @Size(max = 300) String name,
            @Size(max = 200) String publisher,
            @Size(max = 20) String isbn,
            LocalDate releaseDate,
            BigDecimal price,
            Integer printRun,
            @Size(max = 4000) String description,
            @Size(max = 2000) String extras) {
    }

    public record ImageDto(UUID id, String url, ImageKind kind, String caption) {
    }

    public record ImageUrlRequest(
            @NotBlank @Size(max = 1000) String url,
            ImageKind kind,
            @Size(max = 300) String caption) {
    }
}
