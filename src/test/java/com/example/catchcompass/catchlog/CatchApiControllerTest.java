package com.example.catchcompass.catchlog;

import com.example.catchcompass.conditions.CatchConditionsRepository;
import com.example.catchcompass.shared.ApiExceptionHandler;
import com.example.catchcompass.species.Species;
import com.example.catchcompass.storage.PhotoStorage;
import com.example.catchcompass.user.CatchCompassUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The web-layer tests, now against the JSON API rather than rendered pages.
 * Same rules, different transport: what is asserted is the error contract the
 * React client depends on.
 */
@WebMvcTest(CatchApiController.class)
@Import(ApiExceptionHandler.class)
class CatchApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatchService catchService;

    @MockitoBean
    private CatchPhotoRepository catchPhotoRepository;

    @MockitoBean
    private CatchConditionsRepository catchConditionsRepository;


    @MockitoBean
    private PhotoStorage photoStorage;

    private static final CatchCompassUser ANGLER =
            new CatchCompassUser(1L, "angler", "irrelevant-hash", true);

    private static String anHourAgo() {
        return LocalDateTime.now().minusHours(1).toString();
    }

    @Test
    void emptySubmissionReportsEveryMissingField() throws Exception {
        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.speciesId").exists())
                .andExpect(jsonPath("$.errors.caughtAt").exists());
    }

    @Test
    void futureCatchTimeIsRejected() throws Exception {
        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf())
                        .param("speciesId", "1")
                        .param("caughtAt", LocalDateTime.now().plusDays(1).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.caughtAt").exists());
    }

    @Test
    void negativeWeightIsRejected() throws Exception {
        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf())
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("weightKg", "-2.5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.weightKg").exists());
    }

    @Test
    void latitudeWithoutLongitudeIsRejected() throws Exception {
        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf())
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("latitude", "44.5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.locationPairComplete").exists());
    }

    @Test
    void outOfRangeWindDirectionIsRejected() throws Exception {
        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf())
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("conditions.windDirectionDegrees", "400"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['conditions.windDirectionDegrees']").exists());
    }

    @Test
    void impossibleWaterTemperatureIsRejected() throws Exception {
        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf())
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("conditions.waterTemperatureC", "300"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['conditions.waterTemperatureC']").exists());
    }

    @Test
    void lureDescriptionWithoutATypeIsRejected() throws Exception {
        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf())
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("lureDescription", "Rapala Shad Rap, firetiger"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lureDescriptionAccompaniedByType").exists());
    }

    @Test
    void rejectedPhotoIsAFieldErrorRatherThanAServerFault() throws Exception {
        given(catchService.create(any(), any()))
                .willThrow(new PhotoUploadException("Photos must be a JPEG or PNG image"));

        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf())
                        .file(new MockMultipartFile(
                                "photo", "notes.txt", "text/plain", "not an image".getBytes()))
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.photo").exists());
    }

    @Test
    void validSubmissionReturnsCreatedWithTheNewCatch() throws Exception {
        Catch saved = aCatch(42L);
        given(catchService.create(any(), any())).willReturn(saved);
        given(catchService.findOwned(eq(42L), any())).willReturn(saved);
        given(catchPhotoRepository.findFirstByCatchRecordIdOrderByIdAsc(42L))
                .willReturn(Optional.empty());

        mockMvc.perform(multipart("/api/catches").with(user(ANGLER)).with(csrf())
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("weightKg", "2.45"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/catches/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.species.commonName").value("Largemouth Bass"));
    }

    @Test
    void anotherUsersCatchReturnsNotFoundRatherThanForbidden() throws Exception {
        given(catchService.findOwned(eq(99L), any()))
                .willThrow(new CatchNotFoundException(99L));

        mockMvc.perform(get("/api/catches/99").with(user(ANGLER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void journalIsReturnedAsSummaries() throws Exception {
        // Built before the stubbing call: aCatch() stubs mocks of its own, and
        // Mockito cannot handle stubbing that begins inside another given(...).
        Catch entry = aCatch(7L);
        given(catchService.findJournal(any())).willReturn(java.util.List.of(entry));
        given(catchPhotoRepository.findCatchIdsWithPhotos(any())).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/catches").with(user(ANGLER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].species").value("Largemouth Bass"))
                .andExpect(jsonPath("$[0].hasPhoto").value(false));
    }

    private static Catch aCatch(Long id) {
        Species species = mock(Species.class);
        given(species.getId()).willReturn(1L);
        given(species.getCommonName()).willReturn("Largemouth Bass");

        Catch catchRecord = mock(Catch.class);
        given(catchRecord.getId()).willReturn(id);
        given(catchRecord.getSpecies()).willReturn(species);
        given(catchRecord.getCaughtAt()).willReturn(Instant.parse("2026-08-16T12:00:00Z"));
        return catchRecord;
    }
}
