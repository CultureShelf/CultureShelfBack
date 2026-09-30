package com.example.cultureshelf.catalog.provider.mangadex;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cultureshelf.providers.mangadex")
public record MangaDexProperties(
        boolean enabled,
        String baseUrl,
        String coversUrl,
        List<String> preferredLocales,
        String userAgent) {
}
