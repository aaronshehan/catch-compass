package com.example.catchcompass.species;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpeciesRepository extends JpaRepository<Species, Long> {

    List<Species> findByActiveTrueOrderByCommonName();
}
