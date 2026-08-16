package com.example.catchcompass.catchlog;

import com.example.catchcompass.TestcontainersConfiguration;
import com.example.catchcompass.species.Species;
import com.example.catchcompass.species.SpeciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against a real PostgreSQL container, so the Flyway migrations, the
 * CHECK constraints and the seeded species are all genuinely exercised.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CatchRepositoryTest {

    private static final Long ALICE = 1L;
    private static final Long BOB = 2L;

    @Autowired
    private CatchRepository catchRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    private Species anySpecies;

    @BeforeEach
    void loadSeededSpecies() {
        List<Species> species = speciesRepository.findByActiveTrueOrderByCommonName();
        assertThat(species)
                .as("V1 migration should have seeded species")
                .isNotEmpty();
        anySpecies = species.get(0);
    }

    @Test
    void journalContainsOnlyTheGivenUsersCatches() {
        catchRepository.save(new Catch(ALICE, anySpecies, Instant.now()));
        catchRepository.save(new Catch(BOB, anySpecies, Instant.now()));

        List<Catch> journal = catchRepository.findJournal(ALICE);

        assertThat(journal).hasSize(1);
        assertThat(journal.get(0).getUserId()).isEqualTo(ALICE);
    }

    @Test
    void journalIsOrderedNewestFirst() {
        Instant now = Instant.now();
        catchRepository.save(new Catch(ALICE, anySpecies, now.minus(2, ChronoUnit.DAYS)));
        catchRepository.save(new Catch(ALICE, anySpecies, now));
        catchRepository.save(new Catch(ALICE, anySpecies, now.minus(1, ChronoUnit.DAYS)));

        List<Catch> journal = catchRepository.findJournal(ALICE);

        assertThat(journal)
                .extracting(Catch::getCaughtAt)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void ownerCanLoadTheirOwnCatch() {
        Catch saved = catchRepository.save(new Catch(ALICE, anySpecies, Instant.now()));

        Optional<Catch> found = catchRepository.findOwned(saved.getId(), ALICE);

        assertThat(found).isPresent();
    }

    @Test
    void anotherUsersCatchIsInvisibleEvenWithItsRealId() {
        Catch bobsCatch = catchRepository.save(new Catch(BOB, anySpecies, Instant.now()));

        Optional<Catch> found = catchRepository.findOwned(bobsCatch.getId(), ALICE);

        assertThat(found)
                .as("ownership must be enforced by the query, not by the user interface")
                .isEmpty();
    }

    @Test
    void timestampsArePopulatedOnSave() {
        Catch saved = catchRepository.save(new Catch(ALICE, anySpecies, Instant.now()));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
