package com.example.catchcompass.lure;

import com.example.catchcompass.shared.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/lures")
public class LureApiController {

    private final LureService lureService;

    public LureApiController(LureService lureService) {
        this.lureService = lureService;
    }

    @GetMapping
    public List<LureView> tackleBox() {
        return lureService.findTackleBox(CurrentUser.DEV_USER_ID).stream()
                .map(LureView::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<LureView> create(@Valid @RequestBody LureForm form) {
        Lure saved = lureService.create(CurrentUser.DEV_USER_ID, form);
        return ResponseEntity
                .created(URI.create("/api/lures/" + saved.getId()))
                .body(LureView.from(saved));
    }

    /**
     * The enum values, so the frontend builds its dropdowns from the server
     * rather than hardcoding a list that drifts out of sync with the CHECK
     * constraints in the migrations.
     */
    @GetMapping("/options")
    public LureOptions options() {
        return new LureOptions(
                Arrays.stream(LureType.values()).map(Enum::name).toList(),
                Arrays.stream(LurePresentation.values()).map(Enum::name).toList());
    }

    public record LureOptions(List<String> types, List<String> presentations) {
    }

    public record LureView(
            Long id,
            String displayName,
            String type,
            String brand,
            String model,
            String color,
            String size,
            BigDecimal weightGrams,
            String presentation,
            String notes) {

        static LureView from(Lure lure) {
            return new LureView(
                    lure.getId(),
                    lure.getDisplayName(),
                    lure.getLureType().name(),
                    lure.getBrand(),
                    lure.getModel(),
                    lure.getColor(),
                    lure.getSize(),
                    lure.getWeightGrams(),
                    lure.getPresentation() == null ? null : lure.getPresentation().name(),
                    lure.getNotes());
        }
    }
}
