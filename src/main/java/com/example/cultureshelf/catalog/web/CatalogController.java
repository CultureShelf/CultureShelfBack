package com.example.cultureshelf.catalog.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cultureshelf.catalog.provider.CatalogProviderRegistry;
import com.example.cultureshelf.catalog.service.SeriesService;
import com.example.cultureshelf.catalog.web.CatalogDtos.ProviderDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.SearchResultDto;

/** Recherche dans les catalogues externes, avant import. */
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogProviderRegistry providers;
    private final SeriesService seriesService;

    public CatalogController(CatalogProviderRegistry providers, SeriesService seriesService) {
        this.providers = providers;
        this.seriesService = seriesService;
    }

    @GetMapping("/providers")
    public List<ProviderDto> providers() {
        return providers.all().stream().map(p -> new ProviderDto(p.source(), p.displayName())).toList();
    }

    @GetMapping("/search")
    public List<SearchResultDto> search(@RequestParam String source, @RequestParam("q") String query,
            @RequestParam(defaultValue = "20") int limit) {
        if (query.isBlank()) {
            return List.of();
        }
        return seriesService.search(source, query, Math.clamp(limit, 1, 100));
    }
}
