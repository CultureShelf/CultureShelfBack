package com.example.cultureshelf.catalog.provider;

import java.util.List;

import com.example.cultureshelf.catalog.model.MediaKind;

public record ExternalSeries(
        String source,
        String externalId,
        MediaKind mediaKind,
        String title,
        String originalTitle,
        List<String> authors,
        String description,
        String coverUrl,
        Integer year,
        String status) {
}
