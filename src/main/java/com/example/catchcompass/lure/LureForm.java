package com.example.catchcompass.lure;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class LureForm {

    @NotNull(message = "Choose a lure type")
    private LureType lureType;

    @Size(max = 100, message = "Brand must be 100 characters or fewer")
    private String brand;

    @Size(max = 100, message = "Model must be 100 characters or fewer")
    private String model;

    @Size(max = 60, message = "Colour must be 60 characters or fewer")
    private String color;

    @Size(max = 40, message = "Size must be 40 characters or fewer")
    private String size;

    @Positive(message = "Weight must be greater than zero")
    @DecimalMax(value = "99999.99", message = "Weight is unrealistically large")
    private BigDecimal weightGrams;

    private LurePresentation presentation;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;

    public LureType getLureType() { return lureType; }
    public void setLureType(LureType v) { this.lureType = v; }

    public String getBrand() { return brand; }
    public void setBrand(String v) { this.brand = v; }

    public String getModel() { return model; }
    public void setModel(String v) { this.model = v; }

    public String getColor() { return color; }
    public void setColor(String v) { this.color = v; }

    public String getSize() { return size; }
    public void setSize(String v) { this.size = v; }

    public BigDecimal getWeightGrams() { return weightGrams; }
    public void setWeightGrams(BigDecimal v) { this.weightGrams = v; }

    public LurePresentation getPresentation() { return presentation; }
    public void setPresentation(LurePresentation v) { this.presentation = v; }

    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
}
