package com.example.cultureshelf.catalog.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "volume")
public class Volume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id")
    private Series series;

    /** Numero affiche ("1", "10.5", "Tome special"...). */
    @Column(name = "volume_number", nullable = false, length = 40)
    private String number;

    /** Cle de tri numerique derivee du numero. */
    @Column(nullable = false)
    private double sortKey;

    @Column(length = 500)
    private String title;

    @Column(length = 1000)
    private String coverUrl;

    @OneToMany(mappedBy = "volume", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("type ASC, name ASC")
    private List<Variant> variants = new ArrayList<>();

    public void addVariant(Variant variant) {
        variant.setVolume(this);
        variants.add(variant);
    }

    public static double sortKeyOf(String number) {
        if (number == null) {
            return Double.MAX_VALUE;
        }
        try {
            return Double.parseDouble(number.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    public UUID getId() { return id; }
    public Series getSeries() { return series; }
    public void setSeries(Series series) { this.series = series; }
    public String getNumber() { return number; }
    public void setNumber(String number) {
        this.number = number;
        this.sortKey = sortKeyOf(number);
    }
    public double getSortKey() { return sortKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public List<Variant> getVariants() { return variants; }
}
