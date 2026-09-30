package com.example.cultureshelf.catalog.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Serie importee depuis un fournisseur externe (source + externalId), ou creee a la main (source null).
 */
@Entity
@Table(name = "series", uniqueConstraints = @UniqueConstraint(columnNames = {"source", "external_id"}))
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaKind mediaKind = MediaKind.MANGA;

    @Column(length = 40)
    private String source;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)
    private String originalTitle;

    @Column(length = 500)
    private String authors;

    @Column(length = 10000)
    private String description;

    @Column(length = 1000)
    private String coverUrl;

    @Column(name = "publication_year")
    private Integer year;

    @Column(length = 40)
    private String status;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortKey ASC")
    private List<Volume> volumes = new ArrayList<>();

    public void addVolume(Volume volume) {
        volume.setSeries(this);
        volumes.add(volume);
    }

    public UUID getId() { return id; }
    public MediaKind getMediaKind() { return mediaKind; }
    public void setMediaKind(MediaKind mediaKind) { this.mediaKind = mediaKind; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Volume> getVolumes() { return volumes; }
}
