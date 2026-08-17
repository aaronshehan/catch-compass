package com.example.catchcompass.catchlog;

import com.example.catchcompass.conditions.ConditionsForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * What the browser sends when logging a catch.
 *
 * <p>Deliberately separate from the {@link Catch} entity: this holds raw,
 * possibly invalid user input, while the entity may only ever hold valid data.
 */
public class CatchForm {

    @NotNull(message = "Choose a species")
    private Long speciesId;

    @NotNull(message = "Enter when you caught it")
    @PastOrPresent(message = "A catch cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime caughtAt;

    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Positive(message = "Accuracy must be greater than zero")
    @DecimalMax(value = "999999.99", message = "Accuracy is unrealistically large")
    private BigDecimal locationAccuracyMeters;

    /**
     * Set by the browser when a GPS reading succeeds. Not user-editable.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant locationRecordedAt;

    @Positive(message = "Weight must be greater than zero")
    @DecimalMax(value = "999.999", message = "Weight must be under 1000 kg")
    private BigDecimal weightKg;

    @Positive(message = "Length must be greater than zero")
    @DecimalMax(value = "9999.99", message = "Length is unrealistically large")
    private BigDecimal lengthCm;

    @Positive(message = "Circumference must be greater than zero")
    @DecimalMax(value = "9999.99", message = "Circumference is unrealistically large")
    private BigDecimal circumferenceCm;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;

    /**
     * Optional. Content is validated in CatchPhotoService rather than by an
     * annotation, because proving a file is really an image means decoding it.
     */
    private MultipartFile photo;

    /** Optional: not every catch comes on a lure from the tackle box. */
    private Long lureId;

    /**
     * Nested so the conditions fields validate as part of this form. Never null,
     * so Thymeleaf can bind to it on a blank form.
     */
    @Valid
    private ConditionsForm conditions = new ConditionsForm();

    public Long getSpeciesId() { return speciesId; }
    public void setSpeciesId(Long speciesId) { this.speciesId = speciesId; }

    public LocalDateTime getCaughtAt() { return caughtAt; }
    public void setCaughtAt(LocalDateTime caughtAt) { this.caughtAt = caughtAt; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getLocationAccuracyMeters() { return locationAccuracyMeters; }
    public void setLocationAccuracyMeters(BigDecimal locationAccuracyMeters) {
        this.locationAccuracyMeters = locationAccuracyMeters;
    }

    public Instant getLocationRecordedAt() { return locationRecordedAt; }
    public void setLocationRecordedAt(Instant locationRecordedAt) {
        this.locationRecordedAt = locationRecordedAt;
    }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getLengthCm() { return lengthCm; }
    public void setLengthCm(BigDecimal lengthCm) { this.lengthCm = lengthCm; }

    public BigDecimal getCircumferenceCm() { return circumferenceCm; }
    public void setCircumferenceCm(BigDecimal circumferenceCm) { this.circumferenceCm = circumferenceCm; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public MultipartFile getPhoto() { return photo; }
    public void setPhoto(MultipartFile photo) { this.photo = photo; }

    public Long getLureId() { return lureId; }
    public void setLureId(Long lureId) { this.lureId = lureId; }

    public ConditionsForm getConditions() { return conditions; }
    public void setConditions(ConditionsForm conditions) { this.conditions = conditions; }
}
