package com.example.catchcompass.lure;

import com.example.catchcompass.TestcontainersConfiguration;
import com.example.catchcompass.catchlog.Catch;
import com.example.catchcompass.catchlog.CatchRepository;
import com.example.catchcompass.species.Species;
import com.example.catchcompass.species.SpeciesRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The point of the snapshot: history must not change when the tackle box does.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class CatchLureSnapshotTest {

    private static final Long ALICE = 1L;
    private static final Long BOB = 2L;

    @Autowired
    private CatchRepository catchRepository;

    @Autowired
    private LureRepository lureRepository;

    @Autowired
    private CatchLureSnapshotRepository snapshotRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private EntityManager entityManager;

    private Species anySpecies;

    @BeforeEach
    void loadSeededSpecies() {
        anySpecies = speciesRepository.findByActiveTrueOrderByCommonName().get(0);
    }

    private Lure aLure(Long userId, String brand, String color) {
        Lure lure = new Lure(userId, LureType.CRANKBAIT);
        lure.setBrand(brand);
        lure.setModel("Shad Rap");
        lure.setColor(color);
        lure.setWeightGrams(new BigDecimal("12.50"));
        return lureRepository.save(lure);
    }

    @Test
    void editingTheLureLeavesThePastAlone() {
        Lure lure = aLure(ALICE, "Rapala", "Firetiger");
        Catch catchRecord = catchRepository.save(new Catch(ALICE, anySpecies, Instant.now()));
        snapshotRepository.save(CatchLureSnapshot.copyOf(catchRecord, lure));
        entityManager.flush();

        lure.setColor("Blue Chrome");
        lure.setBrand("Something Else");
        lureRepository.save(lure);
        entityManager.flush();
        entityManager.clear();

        CatchLureSnapshot snapshot = snapshotRepository.findById(catchRecord.getId()).orElseThrow();
        assertThat(snapshot.getColor()).isEqualTo("Firetiger");
        assertThat(snapshot.getBrand()).isEqualTo("Rapala");
    }

    @Test
    void deletingTheLureLeavesTheSnapshotIntact() {
        Lure lure = aLure(ALICE, "Rapala", "Firetiger");
        Catch catchRecord = catchRepository.save(new Catch(ALICE, anySpecies, Instant.now()));
        snapshotRepository.save(CatchLureSnapshot.copyOf(catchRecord, lure));
        entityManager.flush();

        lureRepository.delete(lure);
        entityManager.flush();
        entityManager.clear();

        CatchLureSnapshot snapshot = snapshotRepository.findById(catchRecord.getId()).orElseThrow();
        assertThat(snapshot.getBrand())
                .as("the copied fields are the historical record")
                .isEqualTo("Rapala");
        assertThat(snapshot.getLureId())
                .as("ON DELETE SET NULL clears only the convenience reference")
                .isNull();
    }

    @Test
    void tackleBoxContainsOnlyTheGivenUsersLures() {
        aLure(ALICE, "Rapala", "Firetiger");
        aLure(BOB, "Mepps", "Silver");

        assertThat(lureRepository.findByUserIdAndActiveTrueOrderByLureTypeAscBrandAsc(ALICE))
                .singleElement()
                .satisfies(lure -> assertThat(lure.getBrand()).isEqualTo("Rapala"));
    }

    @Test
    void anotherUsersLureCannotBeLookedUp() {
        Lure bobsLure = aLure(BOB, "Mepps", "Silver");

        assertThat(lureRepository.findByIdAndUserId(bobsLure.getId(), ALICE)).isEmpty();
    }
}
