package com.example.catchcompass.conditions;

import com.example.catchcompass.catchlog.Catch;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "catch_conditions")
public class CatchConditions {

    /**
     * Not generated. This table shares its primary key with catches, so the id
     * is the catch's id: see {@code @MapsId} below.
     */
    @Id
    private Long catchId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "catch_id")
    private Catch catchRecord;

    // Explicit names: Hibernate maps a trailing capital as "air_temperaturec",
    // because it only inserts an underscore when the capital is followed by a
    // lowercase letter. Everything else on this entity converts correctly.
    @Column(name = "air_temperature_c", precision = 4, scale = 1)
    private BigDecimal airTemperatureC;

    @Column(name = "water_temperature_c", precision = 4, scale = 1)
    private BigDecimal waterTemperatureC;

    @Column(precision = 5, scale = 2)
    private BigDecimal windSpeedMetersPerSecond;

    private Integer windDirectionDegrees;

    @Column(precision = 5, scale = 2)
    private BigDecimal tideHeightMeters;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TideState tideState;

    @Column(precision = 6, scale = 1)
    private BigDecimal barometricPressureHpa;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SkyCondition skyCondition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConditionsSource conditionsSource;

    private Instant observedAt;

    protected CatchConditions() {
        // JPA requires a no-argument constructor; not for your use
    }

    public CatchConditions(Catch catchRecord, ConditionsSource conditionsSource) {
        this.catchRecord = catchRecord;
        this.conditionsSource = conditionsSource;
    }

    /**
     * Converts the stored bearing into a compass label for display.
     *
     * <p>Degrees are stored because they are exact and sortable; "NE" is a
     * presentation concern, so it is derived rather than saved.
     */
    public String getWindDirectionLabel() {
        if (windDirectionDegrees == null) {
            return null;
        }
        String[] points = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                           "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(windDirectionDegrees / 22.5) % 16;
        return points[index];
    }

    public Long getCatchId() { return catchId; }
    public BigDecimal getAirTemperatureC() { return airTemperatureC; }
    public BigDecimal getWaterTemperatureC() { return waterTemperatureC; }
    public BigDecimal getWindSpeedMetersPerSecond() { return windSpeedMetersPerSecond; }
    public Integer getWindDirectionDegrees() { return windDirectionDegrees; }
    public BigDecimal getTideHeightMeters() { return tideHeightMeters; }
    public TideState getTideState() { return tideState; }
    public BigDecimal getBarometricPressureHpa() { return barometricPressureHpa; }
    public SkyCondition getSkyCondition() { return skyCondition; }
    public ConditionsSource getConditionsSource() { return conditionsSource; }
    public Instant getObservedAt() { return observedAt; }

    public void setAirTemperatureC(BigDecimal v) { this.airTemperatureC = v; }
    public void setWaterTemperatureC(BigDecimal v) { this.waterTemperatureC = v; }
    public void setWindSpeedMetersPerSecond(BigDecimal v) { this.windSpeedMetersPerSecond = v; }
    public void setWindDirectionDegrees(Integer v) { this.windDirectionDegrees = v; }
    public void setTideHeightMeters(BigDecimal v) { this.tideHeightMeters = v; }
    public void setTideState(TideState v) { this.tideState = v; }
    public void setBarometricPressureHpa(BigDecimal v) { this.barometricPressureHpa = v; }
    public void setSkyCondition(SkyCondition v) { this.skyCondition = v; }
    public void setObservedAt(Instant v) { this.observedAt = v; }
}
