package com.example.cultureshelf.catalog.provider.mangadex;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Sous-ensemble des reponses de l'API MangaDex (https://api.mangadex.org/docs/).
 * Les champs localises sont types en Object : MangaDex renvoie parfois [] au lieu de {} quand ils sont vides.
 */
final class MangaDexDtos {

    private MangaDexDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MdList<T>(String result, List<T> data, Integer total) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MdEntity<T>(String result, T data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MdManga(String id, MdMangaAttributes attributes, List<MdRelationship> relationships) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MdMangaAttributes(
            Object title,
            List<Map<String, String>> altTitles,
            Object description,
            String originalLanguage,
            Integer year,
            String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MdRelationship(String id, String type, Map<String, Object> attributes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MdCover(String id, MdCoverAttributes attributes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MdCoverAttributes(String volume, String fileName, String locale) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MdAggregate(String result, Object volumes) {
    }
}
