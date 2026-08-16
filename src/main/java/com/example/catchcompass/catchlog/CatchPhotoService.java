package com.example.catchcompass.catchlog;

import com.example.catchcompass.storage.PhotoStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class CatchPhotoService {

    /**
     * Content type to file extension. Doubles as the allowlist, and matches the
     * CHECK constraint on catch_photos.content_type.
     */
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png");

    private final PhotoStorage photoStorage;
    private final CatchPhotoRepository catchPhotoRepository;

    public CatchPhotoService(PhotoStorage photoStorage, CatchPhotoRepository catchPhotoRepository) {
        this.photoStorage = photoStorage;
        this.catchPhotoRepository = catchPhotoRepository;
    }

    public CatchPhoto attach(Catch catchRecord, MultipartFile file) {
        String declaredType = file.getContentType();
        if (declaredType == null || !ALLOWED_TYPES.containsKey(declaredType)) {
            throw new PhotoUploadException("Photos must be a JPEG or PNG image");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new PhotoUploadException("The photo could not be read. Please try again.");
        }

        // The declared content type comes from the browser and is trivially
        // forged. Decoding the bytes is what actually proves this is an image,
        // and it yields the dimensions at the same time.
        BufferedImage image = decode(content);
        if (image == null) {
            throw new PhotoUploadException("That file is not a readable JPEG or PNG image");
        }

        String storageKey = photoStorage.store(content, ALLOWED_TYPES.get(declaredType));

        return catchPhotoRepository.save(new CatchPhoto(
                catchRecord,
                storageKey,
                declaredType,
                content.length,
                image.getWidth(),
                image.getHeight()));
    }

    private BufferedImage decode(byte[] content) {
        try {
            return ImageIO.read(new ByteArrayInputStream(content));
        } catch (IOException e) {
            return null;
        }
    }
}
