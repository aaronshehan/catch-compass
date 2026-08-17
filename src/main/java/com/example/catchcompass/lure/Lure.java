package com.example.catchcompass.lure;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * An entry in the angler's personal tackle box. Editable and deletable, which
 * is exactly why catches never point at it directly: see {@link CatchLureSnapshot}.
 */
@Entity
@Table(name = "lures")
public class Lure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private LureType lureType;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(length = 60)
    private String color;

    @Column(length = 40)
    private String size;

    @Column(precision = 7, scale = 2)
    private BigDecimal weightGrams;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private LurePresentation presentation;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Lure() {
        // JPA requires a no-argument constructor; not for your use
    }

    public Lure(Long userId, LureType lureType) {
        this.userId = userId;
        this.lureType = lureType;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * A human-readable one-liner for dropdowns and lists, built from whichever
     * fields the angler actually filled in.
     */
    public String getDisplayName() {
        StringBuilder name = new StringBuilder();
        if (brand != null && !brand.isBlank()) {
            name.append(brand).append(' ');
        }
        if (model != null && !model.isBlank()) {
            name.append(model).append(' ');
        }
        if (name.isEmpty()) {
            name.append(lureType).append(' ');
        }
        if (color != null && !color.isBlank()) {
            name.append('(').append(color).append(')');
        }
        return name.toString().trim();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LureType getLureType() { return lureType; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public BigDecimal getWeightGrams() { return weightGrams; }
    public LurePresentation getPresentation() { return presentation; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setLureType(LureType v) { this.lureType = v; }
    public void setBrand(String v) { this.brand = v; }
    public void setModel(String v) { this.model = v; }
    public void setColor(String v) { this.color = v; }
    public void setSize(String v) { this.size = v; }
    public void setWeightGrams(BigDecimal v) { this.weightGrams = v; }
    public void setPresentation(LurePresentation v) { this.presentation = v; }
    public void setNotes(String v) { this.notes = v; }
    public void setActive(boolean v) { this.active = v; }
}
