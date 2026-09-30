package com.example.cultureshelf.catalog.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cultureshelf.catalog.model.MediaKind;
import com.example.cultureshelf.catalog.model.Series;
import com.example.cultureshelf.catalog.model.Variant;
import com.example.cultureshelf.catalog.model.VariantImage;
import com.example.cultureshelf.catalog.model.VariantType;
import com.example.cultureshelf.catalog.model.Volume;
import com.example.cultureshelf.catalog.provider.CatalogProvider;
import com.example.cultureshelf.catalog.provider.CatalogProviderRegistry;
import com.example.cultureshelf.catalog.provider.ExternalSeries;
import com.example.cultureshelf.catalog.provider.ExternalVolume;
import com.example.cultureshelf.catalog.repo.SeriesRepository;
import com.example.cultureshelf.catalog.repo.VolumeRepository;
import com.example.cultureshelf.catalog.web.CatalogDtos.SearchResultDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesDetailDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesRequest;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesSummaryDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VolumeDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VolumeRequest;
import com.example.cultureshelf.collection.CollectionItem;
import com.example.cultureshelf.collection.CollectionItemRepository;
import com.example.cultureshelf.collection.CollectionStatus;
import com.example.cultureshelf.collection.SeriesCollectionCount;
import com.example.cultureshelf.common.NotFoundException;
import com.example.cultureshelf.storage.FileStorageService;

@Service
public class SeriesService {

    static final String DEFAULT_VARIANT_NAME = "Édition standard";

    private final CatalogProviderRegistry providers;
    private final SeriesRepository seriesRepository;
    private final VolumeRepository volumeRepository;
    private final CollectionItemRepository collectionRepository;
    private final FileStorageService storage;

    public SeriesService(CatalogProviderRegistry providers, SeriesRepository seriesRepository,
            VolumeRepository volumeRepository, CollectionItemRepository collectionRepository,
            FileStorageService storage) {
        this.providers = providers;
        this.seriesRepository = seriesRepository;
        this.volumeRepository = volumeRepository;
        this.collectionRepository = collectionRepository;
        this.storage = storage;
    }

    // --- Recherche et import depuis un fournisseur externe

    @Transactional(readOnly = true)
    public List<SearchResultDto> search(String source, String query, int limit) {
        List<ExternalSeries> results = providers.get(source).search(query, limit);
        Map<String, UUID> imported = seriesRepository
                .findBySourceAndExternalIdIn(source, results.stream().map(ExternalSeries::externalId).toList())
                .stream()
                .collect(Collectors.toMap(Series::getExternalId, Series::getId));
        return results.stream()
                .map(s -> new SearchResultDto(s.source(), s.externalId(), s.mediaKind(), s.title(), s.originalTitle(),
                        s.authors(), s.description(), s.coverUrl(), s.year(), s.status(),
                        imported.get(s.externalId())))
                .toList();
    }

    /** Importe la serie et ses tomes (idempotent : une serie deja importee est simplement resynchronisee). */
    @Transactional
    public SeriesDetailDto importSeries(String source, String externalId) {
        Series series = seriesRepository.findBySourceAndExternalId(source, externalId).orElse(null);
        if (series == null) {
            CatalogProvider provider = providers.get(source);
            ExternalSeries external = provider.getSeries(externalId)
                    .orElseThrow(() -> new NotFoundException("Serie " + source, externalId));
            series = new Series();
            series.setSource(source);
            series.setExternalId(externalId);
            applyExternal(series, external);
            syncVolumes(series, provider.getVolumes(externalId));
            series = seriesRepository.save(series);
            return detail(series);
        }
        return refresh(series.getId());
    }

    /** Met a jour les metadonnees et ajoute les nouveaux tomes, sans toucher aux donnees collector. */
    @Transactional
    public SeriesDetailDto refresh(UUID id) {
        Series series = find(id);
        if (series.getSource() == null) {
            throw new IllegalArgumentException("Cette serie a ete creee manuellement : rien a synchroniser");
        }
        CatalogProvider provider = providers.get(series.getSource());
        provider.getSeries(series.getExternalId()).ifPresent(external -> applyExternal(series, external));
        syncVolumes(series, provider.getVolumes(series.getExternalId()));
        // flush : les nouveaux tomes doivent avoir un id avant d'etre renvoyes
        return detail(seriesRepository.saveAndFlush(series));
    }

    private static void applyExternal(Series series, ExternalSeries external) {
        series.setMediaKind(external.mediaKind() != null ? external.mediaKind() : MediaKind.OTHER);
        series.setTitle(external.title());
        series.setOriginalTitle(external.originalTitle());
        series.setAuthors(String.join(", ", external.authors()));
        series.setDescription(truncate(external.description(), 10000));
        series.setCoverUrl(external.coverUrl());
        series.setYear(external.year());
        series.setStatus(external.status());
    }

    private static void syncVolumes(Series series, List<ExternalVolume> externalVolumes) {
        Map<String, Volume> existing = series.getVolumes().stream()
                .collect(Collectors.toMap(Volume::getNumber, Function.identity(), (a, b) -> a));
        for (ExternalVolume external : externalVolumes) {
            Volume volume = existing.get(external.number());
            if (volume == null) {
                volume = newVolume(external.number(), null, external.coverUrl());
                series.addVolume(volume);
            } else if (volume.getCoverUrl() == null) {
                volume.setCoverUrl(external.coverUrl());
            }
        }
    }

    private static Volume newVolume(String number, String title, String coverUrl) {
        Volume volume = new Volume();
        volume.setNumber(number);
        volume.setTitle(title);
        volume.setCoverUrl(coverUrl);
        Variant standard = new Variant();
        standard.setType(VariantType.STANDARD);
        standard.setName(DEFAULT_VARIANT_NAME);
        volume.addVariant(standard);
        return volume;
    }

    // --- Series locales

    @Transactional(readOnly = true)
    public List<SeriesSummaryDto> list() {
        Map<UUID, Long> volumeCounts = new HashMap<>();
        for (Object[] row : seriesRepository.countVolumesBySeries()) {
            volumeCounts.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        Map<UUID, Map<CollectionStatus, SeriesCollectionCount>> collectionCounts = collectionRepository
                .countBySeriesAndStatus().stream()
                .collect(Collectors.groupingBy(SeriesCollectionCount::seriesId,
                        Collectors.toMap(SeriesCollectionCount::status, Function.identity())));

        return seriesRepository.findAllByOrderByTitleAsc().stream()
                .map(s -> {
                    Map<CollectionStatus, SeriesCollectionCount> counts = collectionCounts.getOrDefault(s.getId(), Map.of());
                    SeriesCollectionCount owned = counts.get(CollectionStatus.OWNED);
                    SeriesCollectionCount preordered = counts.get(CollectionStatus.PREORDERED);
                    SeriesCollectionCount wishlist = counts.get(CollectionStatus.WISHLIST);
                    return new SeriesSummaryDto(s.getId(), s.getMediaKind(), s.getTitle(), s.getAuthors(),
                            s.getCoverUrl(), volumeCounts.getOrDefault(s.getId(), 0L),
                            owned == null ? 0 : owned.volumes(),
                            owned == null ? 0 : owned.variants(),
                            preordered == null ? 0 : preordered.variants(),
                            wishlist == null ? 0 : wishlist.variants());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public SeriesDetailDto get(UUID id) {
        return detail(find(id));
    }

    @Transactional
    public SeriesDetailDto create(SeriesRequest request) {
        Series series = new Series();
        apply(series, request);
        return detail(seriesRepository.save(series));
    }

    @Transactional
    public SeriesDetailDto update(UUID id, SeriesRequest request) {
        Series series = find(id);
        apply(series, request);
        return detail(series);
    }

    @Transactional
    public void delete(UUID id) {
        Series series = find(id);
        List<Variant> variants = series.getVolumes().stream().flatMap(v -> v.getVariants().stream()).toList();
        List<String> files = variants.stream()
                .flatMap(v -> v.getImages().stream())
                .map(VariantImage::getStoredFile)
                .filter(f -> f != null)
                .toList();
        if (!variants.isEmpty()) {
            collectionRepository.deleteByVariantIds(variants.stream().map(Variant::getId).toList());
        }
        seriesRepository.delete(series);
        files.forEach(storage::delete);
    }

    private static void apply(Series series, SeriesRequest request) {
        series.setTitle(request.title());
        series.setMediaKind(request.mediaKind() != null ? request.mediaKind() : MediaKind.MANGA);
        series.setOriginalTitle(request.originalTitle());
        series.setAuthors(request.authors());
        series.setDescription(request.description());
        series.setCoverUrl(request.coverUrl());
        series.setYear(request.year());
        series.setStatus(request.status());
    }

    // --- Tomes

    @Transactional
    public VolumeDto addVolume(UUID seriesId, VolumeRequest request) {
        Series series = find(seriesId);
        boolean duplicate = series.getVolumes().stream().anyMatch(v -> v.getNumber().equals(request.number()));
        if (duplicate) {
            throw new IllegalArgumentException("Le tome " + request.number() + " existe deja");
        }
        Volume volume = newVolume(request.number(), request.title(), request.coverUrl());
        series.addVolume(volume);
        volumeRepository.saveAndFlush(volume);
        return CatalogMapper.toVolume(volume, Map.of());
    }

    @Transactional
    public VolumeDto updateVolume(UUID volumeId, VolumeRequest request) {
        Volume volume = findVolume(volumeId);
        volume.setNumber(request.number());
        volume.setTitle(request.title());
        volume.setCoverUrl(request.coverUrl());
        return CatalogMapper.toVolume(volume, itemsByVariant(volume.getVariants()));
    }

    @Transactional
    public void deleteVolume(UUID volumeId) {
        Volume volume = findVolume(volumeId);
        List<String> files = volume.getVariants().stream()
                .flatMap(v -> v.getImages().stream())
                .map(VariantImage::getStoredFile)
                .filter(f -> f != null)
                .toList();
        if (!volume.getVariants().isEmpty()) {
            collectionRepository.deleteByVariantIds(volume.getVariants().stream().map(Variant::getId).toList());
        }
        volume.getSeries().getVolumes().remove(volume);
        files.forEach(storage::delete);
    }

    // --- Utilitaires

    private SeriesDetailDto detail(Series series) {
        List<Variant> variants = series.getVolumes().stream().flatMap(v -> v.getVariants().stream()).toList();
        return CatalogMapper.toDetail(series, itemsByVariant(variants));
    }

    private Map<UUID, CollectionItem> itemsByVariant(List<Variant> variants) {
        Set<UUID> ids = variants.stream().map(Variant::getId).filter(id -> id != null).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return collectionRepository.findByVariantIdIn(ids).stream()
                .collect(Collectors.toMap(i -> i.getVariant().getId(), Function.identity()));
    }

    private Series find(UUID id) {
        return seriesRepository.findById(id).orElseThrow(() -> new NotFoundException("Serie", id));
    }

    private Volume findVolume(UUID id) {
        return volumeRepository.findById(id).orElseThrow(() -> new NotFoundException("Tome", id));
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
