package com.example.catchcompass.species;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/species")
public class SpeciesApiController {

    private final SpeciesRepository speciesRepository;

    public SpeciesApiController(SpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    @GetMapping
    public List<SpeciesView> list() {
        return speciesRepository.findByActiveTrueOrderByCommonName().stream()
                .map(SpeciesView::from)
                .toList();
    }

    public record SpeciesView(Long id, String commonName, String scientificName, String waterType) {

        static SpeciesView from(Species species) {
            return new SpeciesView(
                    species.getId(),
                    species.getCommonName(),
                    species.getScientificName(),
                    species.getWaterType().name());
        }
    }
}
