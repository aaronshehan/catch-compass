package com.example.catchcompass.catchlog;

import com.example.catchcompass.conditions.CatchConditions;
import com.example.catchcompass.conditions.CatchConditionsRepository;
import com.example.catchcompass.conditions.ConditionsForm;
import com.example.catchcompass.conditions.ConditionsSource;
import com.example.catchcompass.species.Species;
import com.example.catchcompass.species.SpeciesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatchService {

    private final CatchRepository catchRepository;
    private final SpeciesRepository speciesRepository;
    private final CatchPhotoService catchPhotoService;
    private final CatchConditionsRepository catchConditionsRepository;

    public CatchService(CatchRepository catchRepository,
                        SpeciesRepository speciesRepository,
                        CatchPhotoService catchPhotoService,
                        CatchConditionsRepository catchConditionsRepository) {
        this.catchRepository = catchRepository;
        this.speciesRepository = speciesRepository;
        this.catchPhotoService = catchPhotoService;
        this.catchConditionsRepository = catchConditionsRepository;
    }

    /**
     * All catches belonging to the given user, newest first.
     */
    public List<Catch> findJournal(Long userId) {
        return catchRepository.findJournal(userId);
    }

    /**
     * A single catch, but only if the given user owns it.
     *
     * @throws CatchNotFoundException if it does not exist or belongs to someone else
     */
    public Catch findOwned(Long id, Long userId) {
        return catchRepository.findOwned(id, userId)
                .orElseThrow(() -> new CatchNotFoundException(id));
    }

    @Transactional
    public Catch create(Long userId, CatchForm form) {
        Species species = speciesRepository.findById(form.getSpeciesId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown species id: " + form.getSpeciesId()));

        // The browser sends a local wall-clock time with no zone attached.
        // Interpreting it in the server's zone is a placeholder; see the note
        // in the README about per-user preferences.
        Catch catchRecord = new Catch(
                userId,
                species,
                form.getCaughtAt().atZone(ZoneId.systemDefault()).toInstant());

        catchRecord.setWeightKg(form.getWeightKg());
        catchRecord.setLengthCm(form.getLengthCm());
        catchRecord.setCircumferenceCm(form.getCircumferenceCm());
        catchRecord.setNotes(form.getNotes());
        if (form.getLatitude() != null) {
            catchRecord.setLocation(
                    form.getLatitude(),
                    form.getLongitude(),
                    form.getLocationAccuracyMeters(),
                    form.getLocationRecordedAt());
        } else {
            // Guards the database rule that a reading time needs coordinates:
            // a stale hidden field cannot survive the user clearing the location.
            catchRecord.clearLocation();
        }

        Catch saved = catchRepository.save(catchRecord);

        // Inside the transaction on purpose: if storing the photo fails, the
        // catch row is rolled back too, rather than leaving a photoless record.
        if (form.getPhoto() != null && !form.getPhoto().isEmpty()) {
            catchPhotoService.attach(saved, form.getPhoto());
        }

        saveConditionsIfAnyWereEntered(saved, form.getConditions());

        return saved;
    }

    /**
     * A row of all-nulls is indistinguishable from "not recorded", so no
     * conditions record is created unless the angler actually entered something.
     */
    private void saveConditionsIfAnyWereEntered(Catch catchRecord, ConditionsForm form) {
        if (form == null || !form.hasAnyValue()) {
            return;
        }

        CatchConditions conditions = new CatchConditions(catchRecord, ConditionsSource.MANUAL);
        conditions.setAirTemperatureC(form.getAirTemperatureC());
        conditions.setWaterTemperatureC(form.getWaterTemperatureC());
        conditions.setWindSpeedMetersPerSecond(form.getWindSpeedMetersPerSecond());
        conditions.setWindDirectionDegrees(form.getWindDirectionDegrees());
        conditions.setTideHeightMeters(form.getTideHeightMeters());
        conditions.setTideState(form.getTideState());
        conditions.setBarometricPressureHpa(form.getBarometricPressureHpa());
        conditions.setSkyCondition(form.getSkyCondition());
        conditions.setObservedAt(form.getObservedAt() == null
                ? null
                : form.getObservedAt().atZone(ZoneId.systemDefault()).toInstant());

        catchConditionsRepository.save(conditions);
    }
}
