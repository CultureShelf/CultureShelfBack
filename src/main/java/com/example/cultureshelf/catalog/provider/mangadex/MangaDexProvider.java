package com.example.cultureshelf.catalog.provider.mangadex;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.example.cultureshelf.catalog.model.MediaKind;
import com.example.cultureshelf.catalog.model.Volume;
import com.example.cultureshelf.catalog.provider.CatalogProvider;
import com.example.cultureshelf.catalog.provider.ExternalSeries;
import com.example.cultureshelf.catalog.provider.ExternalVolume;
import com.example.cultureshelf.catalog.provider.mangadex.MangaDexDtos.MdAggregate;
import com.example.cultureshelf.catalog.provider.mangadex.MangaDexDtos.MdCover;
import com.example.cultureshelf.catalog.provider.mangadex.MangaDexDtos.MdEntity;
import com.example.cultureshelf.catalog.provider.mangadex.MangaDexDtos.MdList;
import com.example.cultureshelf.catalog.provider.mangadex.MangaDexDtos.MdManga;
import com.example.cultureshelf.catalog.provider.mangadex.MangaDexDtos.MdMangaAttributes;
import com.example.cultureshelf.catalog.provider.mangadex.MangaDexDtos.MdRelationship;

/**
 * Catalogue MangaDex : series, auteurs et couvertures par tome (dans plusieurs langues).
 * API publique sans cle, limitee a ~5 requetes/s ; un User-Agent explicite est exige.
 */
@Component
@ConditionalOnProperty(prefix = "cultureshelf.providers.mangadex", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MangaDexProvider implements CatalogProvider {

    static final String SOURCE = "MANGADEX";

    private static final int COVER_PAGE_SIZE = 100;
    private static final int MAX_COVERS = 1000;

    private static final ParameterizedTypeReference<MdList<MdManga>> MANGA_LIST = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<MdEntity<MdManga>> MANGA = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<MdList<MdCover>> COVER_LIST = new ParameterizedTypeReference<>() {
    };

    private final MangaDexProperties properties;
    private final RestClient http;

    public MangaDexProvider(MangaDexProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.http = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent())
                .build();
    }

    @Override
    public String source() {
        return SOURCE;
    }

    @Override
    public String displayName() {
        return "MangaDex";
    }

    @Override
    public List<ExternalSeries> search(String query, int limit) {
        MdList<MdManga> response = http.get()
                .uri(b -> b.path("/manga")
                        .queryParam("title", "{title}")
                        .queryParam("limit", limit)
                        .queryParam("includes[]", "cover_art", "author", "artist")
                        .queryParam("order[relevance]", "desc")
                        .build(query))
                .retrieve()
                .body(MANGA_LIST);
        if (response == null || response.data() == null) {
            return List.of();
        }
        return response.data().stream().map(this::toExternal).toList();
    }

    @Override
    public Optional<ExternalSeries> getSeries(String externalId) {
        try {
            MdEntity<MdManga> response = http.get()
                    .uri(b -> b.path("/manga/{id}")
                            .queryParam("includes[]", "cover_art", "author", "artist")
                            .build(externalId))
                    .retrieve()
                    .body(MANGA);
            return Optional.ofNullable(response).map(MdEntity::data).map(this::toExternal);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ExternalVolume> getVolumes(String externalId) {
        // Les couvertures donnent les tomes publies + leur visuel ; l'agregat complete les tomes sans couverture.
        Map<String, List<MdCover>> coversByVolume = new LinkedHashMap<>();
        for (MdCover cover : fetchCovers(externalId)) {
            String volume = normalizeVolume(cover.attributes() == null ? null : cover.attributes().volume());
            if (volume != null) {
                coversByVolume.computeIfAbsent(volume, k -> new ArrayList<>()).add(cover);
            }
        }
        Set<String> volumes = new LinkedHashSet<>(coversByVolume.keySet());
        volumes.addAll(fetchAggregateVolumes(externalId));

        return volumes.stream()
                .sorted(Comparator.comparingDouble(Volume::sortKeyOf).thenComparing(Comparator.naturalOrder()))
                .map(volume -> new ExternalVolume(volume,
                        bestCover(coversByVolume.getOrDefault(volume, List.of()))
                                .map(cover -> coverUrl(externalId, cover.attributes().fileName()))
                                .orElse(null)))
                .toList();
    }

    private List<MdCover> fetchCovers(String mangaId) {
        List<MdCover> covers = new ArrayList<>();
        int offset = 0;
        int total;
        do {
            final int pageOffset = offset;
            MdList<MdCover> page = http.get()
                    .uri(b -> b.path("/cover")
                            .queryParam("manga[]", "{id}")
                            .queryParam("limit", COVER_PAGE_SIZE)
                            .queryParam("offset", pageOffset)
                            .queryParam("order[volume]", "asc")
                            .build(mangaId))
                    .retrieve()
                    .body(COVER_LIST);
            if (page == null || page.data() == null || page.data().isEmpty()) {
                break;
            }
            covers.addAll(page.data());
            total = page.total() == null ? 0 : page.total();
            offset += COVER_PAGE_SIZE;
        } while (offset < total && offset < MAX_COVERS);
        return covers;
    }

    private Set<String> fetchAggregateVolumes(String mangaId) {
        MdAggregate aggregate = http.get()
                .uri("/manga/{id}/aggregate", mangaId)
                .retrieve()
                .body(MdAggregate.class);
        Set<String> volumes = new LinkedHashSet<>();
        // "volumes" est un objet indexe par numero de tome... ou un tableau vide.
        if (aggregate != null && aggregate.volumes() instanceof Map<?, ?> map) {
            map.keySet().forEach(key -> {
                String volume = normalizeVolume(String.valueOf(key));
                if (volume != null) {
                    volumes.add(volume);
                }
            });
        }
        return volumes;
    }

    private Optional<MdCover> bestCover(List<MdCover> covers) {
        List<String> locales = properties.preferredLocales();
        return covers.stream()
                .filter(c -> c.attributes() != null && c.attributes().fileName() != null)
                .min(Comparator.comparingInt(c -> {
                    int index = locales.indexOf(c.attributes().locale());
                    return index < 0 ? locales.size() : index;
                }));
    }

    private static String normalizeVolume(String volume) {
        if (volume == null || volume.isBlank() || "none".equalsIgnoreCase(volume)) {
            return null;
        }
        return volume.trim();
    }

    private ExternalSeries toExternal(MdManga manga) {
        MdMangaAttributes attributes = manga.attributes();
        List<MdRelationship> relationships = manga.relationships() == null ? List.of() : manga.relationships();

        String coverFile = relationships.stream()
                .filter(r -> "cover_art".equals(r.type()) && r.attributes() != null)
                .map(r -> (String) r.attributes().get("fileName"))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        List<String> authors = relationships.stream()
                .filter(r -> ("author".equals(r.type()) || "artist".equals(r.type())) && r.attributes() != null)
                .map(r -> (String) r.attributes().get("name"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return new ExternalSeries(
                SOURCE,
                manga.id(),
                mediaKindOf(attributes.originalLanguage()),
                pickTitle(attributes),
                pickOriginalTitle(attributes),
                authors,
                pickLocalized(attributes.description()),
                coverFile == null ? null : coverUrl(manga.id(), coverFile),
                attributes.year(),
                attributes.status());
    }

    private String coverUrl(String mangaId, String fileName) {
        return properties.coversUrl() + "/" + mangaId + "/" + fileName + ".512.jpg";
    }

    private String pickTitle(MdMangaAttributes attributes) {
        Map<String, String> titles = new LinkedHashMap<>(asStringMap(attributes.title()));
        if (attributes.altTitles() != null) {
            attributes.altTitles().forEach(alt -> alt.forEach(titles::putIfAbsent));
        }
        for (String locale : properties.preferredLocales()) {
            // Le titre dans la langue d'origine (kanji...) est garde pour originalTitle : on prefere le romaji principal.
            if (titles.containsKey(locale) && !locale.equals(attributes.originalLanguage())) {
                return titles.get(locale);
            }
        }
        return asStringMap(attributes.title()).values().stream().findFirst()
                .orElse(titles.values().stream().findFirst().orElse("Sans titre"));
    }

    private static String pickOriginalTitle(MdMangaAttributes attributes) {
        String language = attributes.originalLanguage();
        if (language == null || attributes.altTitles() == null) {
            return null;
        }
        return attributes.altTitles().stream()
                .map(alt -> alt.get(language))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String pickLocalized(Object localized) {
        Map<String, String> values = asStringMap(localized);
        for (String locale : properties.preferredLocales()) {
            String value = values.get(locale);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return values.values().stream().filter(v -> v != null && !v.isBlank()).findFirst().orElse(null);
    }

    private static Map<String, String> asStringMap(Object value) {
        Map<String, String> result = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                if (v != null) {
                    result.put(String.valueOf(k), String.valueOf(v));
                }
            });
        }
        return result;
    }

    private static MediaKind mediaKindOf(String originalLanguage) {
        if (originalLanguage == null) {
            return MediaKind.MANGA;
        }
        return switch (originalLanguage) {
            case "ko" -> MediaKind.MANHWA;
            case "zh", "zh-hk" -> MediaKind.MANHUA;
            default -> MediaKind.MANGA;
        };
    }
}
