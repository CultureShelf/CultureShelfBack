package com.example.cultureshelf.catalog.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Une edition physique precise d'un tome : standard, collector, jaquette alternative...
 */
@Entity
@Table(name = "variant")
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volume_id")
    private Volume volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VariantType type = VariantType.STANDARD;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(length = 200)
    private String publisher;

    @Column(length = 20)
    private String isbn;

    private LocalDate releaseDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /** Tirage limite, si connu. */
    private Integer printRun;

    @Column(length = 4000)
    private String description;

    /** Contenu bonus : ex-libris, coffret, marque-page... */
    @Column(length = 2000)
    private String extras;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<VariantImage> images = new ArrayList<>();

    public void addImage(VariantImage image) {
        image.setVariant(this);
        image.setSortOrder(images.size());
        images.add(image);
    }

    public UUID getId() { return id; }
    public Volume getVolume() { return volume; }
    public void setVolume(Volume volume) { this.volume = volume; }
    public VariantType getType() { return type; }
    public void setType(VariantType type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getPrintRun() { return printRun; }
    public void setPrintRun(Integer printRun) { this.printRun = printRun; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getExtras() { return extras; }
    public void setExtras(String extras) { this.extras = extras; }
    public List<VariantImage> getImages() { return images; }
}
