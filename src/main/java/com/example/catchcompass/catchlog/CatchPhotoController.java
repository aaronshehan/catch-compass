package com.example.catchcompass.catchlog;

import com.example.catchcompass.shared.CurrentUser;
import com.example.catchcompass.storage.PhotoStorage;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CatchPhotoController {

    private final CatchService catchService;
    private final CatchPhotoRepository catchPhotoRepository;
    private final PhotoStorage photoStorage;

    public CatchPhotoController(CatchService catchService,
                                CatchPhotoRepository catchPhotoRepository,
                                PhotoStorage photoStorage) {
        this.catchService = catchService;
        this.catchPhotoRepository = catchPhotoRepository;
        this.photoStorage = photoStorage;
    }

    @GetMapping("/catches/{catchId}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Long catchId) {
        // Ownership is checked before a single byte is read from storage.
        // Photos are not static resources; this is the only route to them.
        catchService.findOwned(catchId, CurrentUser.DEV_USER_ID);

        CatchPhoto photo = catchPhotoRepository.findFirstByCatchRecordIdOrderByIdAsc(catchId)
                .orElseThrow(() -> new CatchNotFoundException(catchId));

        byte[] content = photoStorage.load(photo.getStorageKey());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .cacheControl(CacheControl.noStore().cachePrivate())
                .body(content);
    }
}
