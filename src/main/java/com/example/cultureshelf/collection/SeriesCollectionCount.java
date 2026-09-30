package com.example.cultureshelf.collection;

import java.util.UUID;

public record SeriesCollectionCount(UUID seriesId, CollectionStatus status, long volumes, long variants) {
}
