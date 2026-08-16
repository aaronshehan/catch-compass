package com.example.catchcompass.species;

import jakarta.persistence.*;

@Entity
@Table(name = "species")
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String commonName;

    @Column(length = 150)
    private String scientificName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WaterType waterType;

    @Column(nullable = false)
    private boolean active = true;

    protected Species() {
        
    }

    public Species(String commonName, String scientificName, WaterType waterType) {
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.waterType = waterType;
    }

    public Long getId() { return id; }
    public String getCommonName() { return commonName; }
    public String getScientificName() { return scientificName; }
    public WaterType getWaterType() { return waterType; }
    public boolean isActive() { return active; }
}