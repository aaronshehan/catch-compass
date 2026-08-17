package com.example.catchcompass.catchlog;

import com.example.catchcompass.conditions.CatchConditionsRepository;
import com.example.catchcompass.lure.CatchLureSnapshotRepository;
import com.example.catchcompass.shared.CurrentUser;
import com.example.catchcompass.storage.PhotoStorage;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/catches")
public class CatchApiController {

    private final CatchService catchService;
    private final CatchPhotoRepository catchPhotoRepository;
    private final CatchConditionsRepository catchConditionsRepository;
    private final CatchLureSnapshotRepository catchLureSnapshotRepository;
    private final PhotoStorage photoStorage;

    public CatchApiController(CatchService catchService,
                              CatchPhotoRepository catchPhotoRepository,
                              CatchConditionsRepository catchConditionsRepository,
                              CatchLureSnapshotRepository catchLureSnapshotRepository,
                              PhotoStorage photoStorage) {
        this.catchService = catchService;
        this.catchPhotoRepository = catchPhotoRepository;
        this.catchConditionsRepository = catchConditionsRepository;
        this.catchLureSnapshotRepository = catchLureSnapshotRepository;
        this.photoStorage = photoStorage;
    }

    @GetMapping
    public List<CatchResponse.Summary> journal() {
        List<Catch> catches = catchService.findJournal(CurrentUser.DEV_USER_ID);
        Set<Long> withPhotos = photoIdsFor(catches);

        return catches.stream()
                .map(entry -> CatchResponse.Summary.from(entry, withPhotos.contains(entry.getId())))
                .toList();
    }

    @GetMapping("/{id}")
    public CatchResponse.Detail detail(@PathVariable Long id) {
        Catch catchRecord = catchService.findOwned(id, CurrentUser.DEV_USER_ID);

        return CatchResponse.Detail.from(
                catchRecord,
                catchPhotoRepository.findFirstByCatchRecordIdOrderByIdAsc(id).isPresent(),
                catchLureSnapshotRepository.findById(id).orElse(null),
                catchConditionsRepository.findById(id).orElse(null));
    }

    /**
     * Multipart rather than JSON, because the photo travels with the fields.
     * The browser sends FormData and Spring binds it to the same CatchForm the
     * Thymeleaf form uses, so both frontends share one set of validation rules.
     *
     * <p>No BindingResult parameter: without one, a validation failure throws
     * and ApiExceptionHandler renders it as JSON, which is what the client wants.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CatchResponse.Detail> create(@Valid @ModelAttribute CatchForm form) {
        Catch saved = catchService.create(CurrentUser.DEV_USER_ID, form);

        return ResponseEntity
                .created(URI.create("/api/catches/" + saved.getId()))
                .body(detail(saved.getId()));
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) {
        // Ownership first, always, before a byte is read from storage.
        catchService.findOwned(id, CurrentUser.DEV_USER_ID);

        CatchPhoto photo = catchPhotoRepository.findFirstByCatchRecordIdOrderByIdAsc(id)
                .orElseThrow(() -> new CatchNotFoundException(id));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .cacheControl(CacheControl.noStore().cachePrivate())
                .body(photoStorage.load(photo.getStorageKey()));
    }

    private Set<Long> photoIdsFor(List<Catch> catches) {
        if (catches.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(catchPhotoRepository.findCatchIdsWithPhotos(
                catches.stream().map(Catch::getId).toList()));
    }
}
