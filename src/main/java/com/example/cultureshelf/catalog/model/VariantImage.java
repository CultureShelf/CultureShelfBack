package com.example.cultureshelf.catalog.model;

import java.util.UUID;

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

/** Visuel d'une variante : soit une URL externe, soit un fichier uploade ({@link #storedFile}). */
@Entity
@Table(name = "variant_image")
public class VariantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id")
    private Variant variant;

    @Column(nullable = false, length = 1000)
    private String url;

    /** Nom du fichier dans le stockage local, null si l'image est une URL externe. */
    @Column(length = 100)
    private String storedFile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageKind kind = ImageKind.FRONT;

    @Column(length = 300)
    private String caption;

    @Column(nullable = false)
    private int sortOrder;

    public UUID getId() { return id; }
    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getStoredFile() { return storedFile; }
    public void setStoredFile(String storedFile) { this.storedFile = storedFile; }
    public ImageKind getKind() { return kind; }
    public void setKind(ImageKind kind) { this.kind = kind; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
