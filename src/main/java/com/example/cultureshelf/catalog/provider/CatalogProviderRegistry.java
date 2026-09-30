package com.example.cultureshelf.catalog.provider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class CatalogProviderRegistry {

    private final Map<String, CatalogProvider> providers;

    public CatalogProviderRegistry(List<CatalogProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(CatalogProvider::source, Function.identity()));
    }

    public CatalogProvider get(String source) {
        CatalogProvider provider = providers.get(source);
        if (provider == null) {
            throw new IllegalArgumentException("Fournisseur inconnu : " + source + " (disponibles : " + providers.keySet() + ")");
        }
        return provider;
    }

    public List<CatalogProvider> all() {
        return List.copyOf(providers.values());
    }
}
