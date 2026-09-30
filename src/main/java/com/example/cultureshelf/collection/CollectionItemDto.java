package com.example.cultureshelf.collection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CollectionItemDto(
        UUID id,
        UUID variantId,
        CollectionStatus status,
        ItemCondition condition,
        int quantity,
        LocalDate acquiredAt,
        BigDecimal pricePaid,
        String notes) {

    public static CollectionItemDto of(CollectionItem item) {
        if (item == null) {
            return null;
        }
        return new CollectionItemDto(item.getId(), item.getVariant().getId(), item.getStatus(), item.getCondition(),
                item.getQuantity(), item.getAcquiredAt(), item.getPricePaid(), item.getNotes());
    }
}
