package com.example.cultureshelf.catalog.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.cultureshelf.catalog.model.Series;

public interface SeriesRepository extends JpaRepository<Series, UUID> {

    Optional<Series> findBySourceAndExternalId(String source, String externalId);

    List<Series> findBySourceAndExternalIdIn(String source, List<String> externalIds);

    List<Series> findAllByOrderByTitleAsc();

    @Query("select s.id, count(v) from Series s left join s.volumes v group by s.id")
    List<Object[]> countVolumesBySeries();
}
