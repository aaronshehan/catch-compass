package com.example.catchcompass.catchlog;

import com.example.catchcompass.conditions.CatchConditionsRepository;
import com.example.catchcompass.lure.CatchLureSnapshotRepository;
import com.example.catchcompass.lure.LureService;
import com.example.catchcompass.species.SpeciesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Exercises the web layer only. No database is started; the service is mocked,
 * so these tests are fast and fail for exactly one reason: controller behaviour.
 */
@WebMvcTest(CatchController.class)
class CatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatchService catchService;

    @MockitoBean
    private SpeciesRepository speciesRepository;

    @MockitoBean
    private CatchPhotoRepository catchPhotoRepository;

    @MockitoBean
    private CatchConditionsRepository catchConditionsRepository;

    @MockitoBean
    private LureService lureService;

    @MockitoBean
    private CatchLureSnapshotRepository catchLureSnapshotRepository;

    private static String anHourAgo() {
        return LocalDateTime.now().minusHours(1).toString();
    }

    @Test
    void formPageRenders() throws Exception {
        mockMvc.perform(get("/catches/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("catches/new"));
    }

    @Test
    void emptySubmissionIsRejectedAndRedisplaysTheForm() throws Exception {
        mockMvc.perform(post("/catches"))
                .andExpect(status().isOk())
                .andExpect(view().name("catches/new"))
                .andExpect(model().attributeHasFieldErrors("catchForm", "speciesId", "caughtAt"));
    }

    @Test
    void futureCatchTimeIsRejected() throws Exception {
        mockMvc.perform(post("/catches")
                        .param("speciesId", "1")
                        .param("caughtAt", LocalDateTime.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("catchForm", "caughtAt"));
    }

    @Test
    void negativeWeightIsRejected() throws Exception {
        mockMvc.perform(post("/catches")
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("weightKg", "-2.5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("catchForm", "weightKg"));
    }

    @Test
    void latitudeWithoutLongitudeIsRejected() throws Exception {
        mockMvc.perform(post("/catches")
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("latitude", "44.5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("catchForm", "longitude"));
    }

    @Test
    void validSubmissionRedirectsToTheSavedCatch() throws Exception {
        Catch saved = mock(Catch.class);
        given(saved.getId()).willReturn(42L);
        given(catchService.create(any(), any())).willReturn(saved);

        mockMvc.perform(post("/catches")
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("weightKg", "2.45"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catches/42"));
    }

    @Test
    void outOfRangeWindDirectionIsRejected() throws Exception {
        mockMvc.perform(post("/catches")
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("conditions.windDirectionDegrees", "360"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "catchForm", "conditions.windDirectionDegrees"));
    }

    @Test
    void impossibleWaterTemperatureIsRejected() throws Exception {
        mockMvc.perform(post("/catches")
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo())
                        .param("conditions.waterTemperatureC", "300"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "catchForm", "conditions.waterTemperatureC"));
    }

    @Test
    void rejectedPhotoBecomesAFieldErrorRatherThanAFailedRequest() throws Exception {
        given(catchService.create(any(), any()))
                .willThrow(new PhotoUploadException("Photos must be a JPEG or PNG image"));

        mockMvc.perform(multipart("/catches")
                        .file(new MockMultipartFile(
                                "photo", "notes.txt", "text/plain", "not an image".getBytes()))
                        .param("speciesId", "1")
                        .param("caughtAt", anHourAgo()))
                .andExpect(status().isOk())
                .andExpect(view().name("catches/new"))
                .andExpect(model().attributeHasFieldErrors("catchForm", "photo"));
    }

    @Test
    void anotherUsersCatchReturnsNotFoundRatherThanForbidden() throws Exception {
        given(catchService.findOwned(eq(99L), any()))
                .willThrow(new CatchNotFoundException(99L));

        mockMvc.perform(get("/catches/99"))
                .andExpect(status().isNotFound());
    }
}
