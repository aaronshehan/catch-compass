package com.example.catchcompass.conditions;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Nested inside CatchForm. Every field is optional: conditions are recorded
 * only if the angler bothers, and a catch is still a catch without them.
 *
 * <p>Ranges mirror the CHECK constraints in V4 so a bad value becomes a field
 * error rather than a 500 from the database.
 */
public class ConditionsForm {

    @DecimalMin(value = "-60", message = "Air temperature must be between -60 and 60 C")
    @DecimalMax(value = "60", message = "Air temperature must be between -60 and 60 C")
    private BigDecimal airTemperatureC;

    @DecimalMin(value = "-5", message = "Water temperature must be between -5 and 45 C")
    @DecimalMax(value = "45", message = "Water temperature must be between -5 and 45 C")
    private BigDecimal waterTemperatureC;

    @DecimalMin(value = "0", message = "Wind speed cannot be negative")
    @DecimalMax(value = "120", message = "Wind speed is unrealistically high")
    private BigDecimal windSpeedMetersPerSecond;

    @Min(value = 0, message = "Wind direction must be between 0 and 359 degrees")
    @Max(value = 359, message = "Wind direction must be between 0 and 359 degrees")
    private Integer windDirectionDegrees;

    @DecimalMin(value = "-5", message = "Tide height must be between -5 and 20 m")
    @DecimalMax(value = "20", message = "Tide height must be between -5 and 20 m")
    private BigDecimal tideHeightMeters;

    private TideState tideState;

    @DecimalMin(value = "800", message = "Pressure must be between 800 and 1100 hPa")
    @DecimalMax(value = "1100", message = "Pressure must be between 800 and 1100 hPa")
    private BigDecimal barometricPressureHpa;

    private SkyCondition skyCondition;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime observedAt;

    /**
     * Maintained by the browser: MANUAL until conditions are fetched, then
     * WEATHER_API, then WEATHER_API_EDITED as soon as any field is touched.
     */
    private ConditionsSource conditionsSource = ConditionsSource.MANUAL;

    /**
     * Whether the angler entered anything at all. Used to decide between saving
     * a conditions row and saving none, rather than storing a row of nulls that
     * is indistinguishable from "not recorded".
     */
    public boolean hasAnyValue() {
        return airTemperatureC != null
                || waterTemperatureC != null
                || windSpeedMetersPerSecond != null
                || windDirectionDegrees != null
                || tideHeightMeters != null
                || tideState != null
                || barometricPressureHpa != null
                || skyCondition != null
                || observedAt != null;
    }

    public BigDecimal getAirTemperatureC() { return airTemperatureC; }
    public void setAirTemperatureC(BigDecimal v) { this.airTemperatureC = v; }

    public BigDecimal getWaterTemperatureC() { return waterTemperatureC; }
    public void setWaterTemperatureC(BigDecimal v) { this.waterTemperatureC = v; }

    public BigDecimal getWindSpeedMetersPerSecond() { return windSpeedMetersPerSecond; }
    public void setWindSpeedMetersPerSecond(BigDecimal v) { this.windSpeedMetersPerSecond = v; }

    public Integer getWindDirectionDegrees() { return windDirectionDegrees; }
    public void setWindDirectionDegrees(Integer v) { this.windDirectionDegrees = v; }

    public BigDecimal getTideHeightMeters() { return tideHeightMeters; }
    public void setTideHeightMeters(BigDecimal v) { this.tideHeightMeters = v; }

    public TideState getTideState() { return tideState; }
    public void setTideState(TideState v) { this.tideState = v; }

    public BigDecimal getBarometricPressureHpa() { return barometricPressureHpa; }
    public void setBarometricPressureHpa(BigDecimal v) { this.barometricPressureHpa = v; }

    public SkyCondition getSkyCondition() { return skyCondition; }
    public void setSkyCondition(SkyCondition v) { this.skyCondition = v; }

    public LocalDateTime getObservedAt() { return observedAt; }
    public void setObservedAt(LocalDateTime v) { this.observedAt = v; }

    public ConditionsSource getConditionsSource() {
        return conditionsSource == null ? ConditionsSource.MANUAL : conditionsSource;
    }

    public void setConditionsSource(ConditionsSource v) { this.conditionsSource = v; }
}
