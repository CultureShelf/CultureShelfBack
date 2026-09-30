package com.example.cultureshelf.collection;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.example.cultureshelf.catalog.model.Variant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Exemplaire d'une variante dans la collection (possede, precommande ou souhaite). */
@Entity
@Table(name = "collection_item")
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", unique = true)
    private Variant variant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollectionStatus status = CollectionStatus.OWNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", length = 20)
    private ItemCondition condition;

    @Column(nullable = false)
    private int quantity = 1;

    private LocalDate acquiredAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }
    public CollectionStatus getStatus() { return status; }
    public void setStatus(CollectionStatus status) { this.status = status; }
    public ItemCondition getCondition() { return condition; }
    public void setCondition(ItemCondition condition) { this.condition = condition; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public LocalDate getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDate acquiredAt) { this.acquiredAt = acquiredAt; }
    public BigDecimal getPricePaid() { return pricePaid; }
    public void setPricePaid(BigDecimal pricePaid) { this.pricePaid = pricePaid; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
}
