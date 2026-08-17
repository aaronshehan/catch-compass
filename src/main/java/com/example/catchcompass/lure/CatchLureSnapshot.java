package com.example.catchcompass.lure;

import com.example.catchcompass.catchlog.Catch;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The lure exactly as it was when the catch was recorded.
 *
 * <p>Nothing here is ever updated. Rename a lure in your tackle box, change its
 * colour, or delete it entirely, and every catch still reports what actually
 * caught the fish. A live reference to {@link Lure} would quietly rewrite
 * history instead.
 */
@Entity
@Table(name = "catch_lure_snapshots")
public class CatchLureSnapshot {

    @Id
    private Long catchId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "catch_id")
    private Catch catchRecord;

    /**
     * Convenience link back to the tackle box entry, for questions like "which
     * other catches used this lure". Null once the lure is deleted; the copied
     * fields below remain intact.
     */
    @Column(name = "lure_id")
    private Long lureId;

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

    @Column(nullable = false)
    private Instant capturedAt;

    protected CatchLureSnapshot() {
        // JPA requires a no-argument constructor; not for your use
    }

    /**
     * Copies the lure's current state onto the catch. Called once, at save time.
     */
    public static CatchLureSnapshot copyOf(Catch catchRecord, Lure lure) {
        CatchLureSnapshot snapshot = new CatchLureSnapshot();
        snapshot.catchRecord = catchRecord;
        snapshot.lureId = lure.getId();
        snapshot.lureType = lure.getLureType();
        snapshot.brand = lure.getBrand();
        snapshot.model = lure.getModel();
        snapshot.color = lure.getColor();
        snapshot.size = lure.getSize();
        snapshot.weightGrams = lure.getWeightGrams();
        snapshot.presentation = lure.getPresentation();
        return snapshot;
    }

    @PrePersist
    void onCreate() {
        this.capturedAt = Instant.now();
    }

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

    public Long getCatchId() { return catchId; }
    public Long getLureId() { return lureId; }
    public LureType getLureType() { return lureType; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public BigDecimal getWeightGrams() { return weightGrams; }
    public LurePresentation getPresentation() { return presentation; }
    public Instant getCapturedAt() { return capturedAt; }
}
