package com.example.catchcompass.catchlog;

import com.example.catchcompass.storage.PhotoStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Plain unit tests: no Spring, no database, no container. The upload rules are
 * pure logic, so they should be testable in milliseconds.
 */
class CatchPhotoServiceTest {

    private PhotoStorage photoStorage;
    private CatchPhotoRepository catchPhotoRepository;
    private CatchPhotoService service;

    @BeforeEach
    void setUp() {
        photoStorage = mock(PhotoStorage.class);
        catchPhotoRepository = mock(CatchPhotoRepository.class);
        given(photoStorage.store(any(), any())).willReturn("2026/08/16/generated-key.png");
        given(catchPhotoRepository.save(any())).willAnswer(call -> call.getArgument(0));
        service = new CatchPhotoService(photoStorage, catchPhotoRepository);
    }

    @Test
    void rejectsAFileWhoseTypeIsNotAllowed() {
        MultipartFile file = new MockMultipartFile(
                "photo", "notes.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> service.attach(null, file))
                .isInstanceOf(PhotoUploadException.class)
                .hasMessageContaining("JPEG or PNG");
    }

    /**
     * The important one: the browser claims this is a JPEG and it is not.
     * A content-type check alone would let it through.
     */
    @Test
    void rejectsANonImageThatClaimsToBeAnImage() {
        MultipartFile file = new MockMultipartFile(
                "photo", "fish.jpg", "image/jpeg", "<?php system($_GET['c']); ?>".getBytes());

        assertThatThrownBy(() -> service.attach(null, file))
                .isInstanceOf(PhotoUploadException.class)
                .hasMessageContaining("not a readable");
    }

    @Test
    void acceptsARealImageAndRecordsItsDimensions() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "photo", "fish.png", "image/png", pngOfSize(640, 480));

        CatchPhoto stored = service.attach(null, file);

        assertThat(stored.getWidthPx()).isEqualTo(640);
        assertThat(stored.getHeightPx()).isEqualTo(480);
        assertThat(stored.getContentType()).isEqualTo("image/png");
        assertThat(stored.getStorageKey())
                .as("the storage key must be generated, never taken from the upload")
                .doesNotContain("fish.png");
    }

    private static byte[] pngOfSize(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
