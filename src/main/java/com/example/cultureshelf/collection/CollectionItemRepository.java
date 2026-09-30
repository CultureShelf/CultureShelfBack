package com.example.cultureshelf.collection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionItemRepository extends JpaRepository<CollectionItem, UUID> {

    Optional<CollectionItem> findByVariantId(UUID variantId);

    List<CollectionItem> findByVariantIdIn(Collection<UUID> variantIds);

    /** Nombre de tomes distincts et de variantes par serie et par statut. */
    @Query("""
            select new com.example.cultureshelf.collection.SeriesCollectionCount(
                vol.series.id, c.status, count(distinct vol.id), count(c))
            from CollectionItem c join c.variant v join v.volume vol
            group by vol.series.id, c.status
            """)
    List<SeriesCollectionCount> countBySeriesAndStatus();

    @Query("""
            select c from CollectionItem c
            join fetch c.variant v join fetch v.volume vol join fetch vol.series
            where (:status is null or c.status = :status)
            order by vol.series.title, vol.sortKey
            """)
    List<CollectionItem> findAllWithCatalog(@Param("status") CollectionStatus status);

    @Modifying
    @Query("delete from CollectionItem c where c.variant.id in :variantIds")
    void deleteByVariantIds(@Param("variantIds") Collection<UUID> variantIds);
}
