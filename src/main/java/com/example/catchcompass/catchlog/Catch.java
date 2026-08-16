package com.example.catchcompass.catchlog;

import com.example.catchcompass.species.Species;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "catches")
public class Catch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @Column(nullable = false)
    private Instant caughtAt;

    @Column(precision = 8, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(precision = 8, scale = 2)
    private BigDecimal locationAccuracyMeters;

    /**
     * When the device produced the reading, which is not the same as when the
     * fish was caught or when the form was submitted.
     */
    private Instant locationRecordedAt;

    @Column(precision = 6, scale = 3)
    private BigDecimal weightKg;

    @Column(precision = 6, scale = 2)
    private BigDecimal lengthCm;

    @Column(precision = 6, scale = 2)
    private BigDecimal circumferenceCm;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Catch() {
        // JPA requires a no-argument constructor; not for your use
    }

    public Catch(Long userId, Species species, Instant caughtAt) {
        this.userId = userId;
        this.species = species;
        this.caughtAt = caughtAt;
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
     * Latitude and longitude are stored as a pair, matching the database
     * constraint that requires both to be present or both absent.
     */
    public void setLocation(BigDecimal latitude,
                            BigDecimal longitude,
                            BigDecimal accuracyMeters,
                            Instant recordedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationAccuracyMeters = accuracyMeters;
        this.locationRecordedAt = recordedAt;
    }

    public void clearLocation() {
        this.latitude = null;
        this.longitude = null;
        this.locationAccuracyMeters = null;
        this.locationRecordedAt = null;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Species getSpecies() { return species; }
    public Instant getCaughtAt() { return caughtAt; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getLocationAccuracyMeters() { return locationAccuracyMeters; }
    public Instant getLocationRecordedAt() { return locationRecordedAt; }
    public BigDecimal getWeightKg() { return weightKg; }
    public BigDecimal getLengthCm() { return lengthCm; }
    public BigDecimal getCircumferenceCm() { return circumferenceCm; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setSpecies(Species species) { this.species = species; }
    public void setCaughtAt(Instant caughtAt) { this.caughtAt = caughtAt; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public void setLengthCm(BigDecimal lengthCm) { this.lengthCm = lengthCm; }
    public void setCircumferenceCm(BigDecimal circumferenceCm) { this.circumferenceCm = circumferenceCm; }
    public void setNotes(String notes) { this.notes = notes; }
}
