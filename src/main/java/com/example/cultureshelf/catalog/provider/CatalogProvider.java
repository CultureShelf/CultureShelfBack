package com.example.cultureshelf.catalog.provider;

import java.util.List;
import java.util.Optional;

/**
 * Source de donnees externe (MangaDex, et demain Google Books, AniList, ComicVine...).
 * Chaque implementation est un bean Spring : l'ajouter suffit a l'exposer dans l'API.
 */
public interface CatalogProvider {

    /** Identifiant stable, stocke en base avec chaque serie importee. */
    String source();

    String displayName();

    List<ExternalSeries> search(String query, int limit);

    Optional<ExternalSeries> getSeries(String externalId);

    /** Tomes connus du fournisseur, tries par numero. */
    List<ExternalVolume> getVolumes(String externalId);
}
