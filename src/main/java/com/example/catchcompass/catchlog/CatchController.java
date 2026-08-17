package com.example.catchcompass.catchlog;

import com.example.catchcompass.conditions.CatchConditionsRepository;
import com.example.catchcompass.conditions.SkyCondition;
import com.example.catchcompass.conditions.TideState;
import com.example.catchcompass.lure.CatchLureSnapshotRepository;
import com.example.catchcompass.lure.Lure;
import com.example.catchcompass.lure.LureService;
import com.example.catchcompass.shared.CurrentUser;
import com.example.catchcompass.species.Species;
import com.example.catchcompass.species.SpeciesRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@Controller
public class CatchController {

    private final CatchService catchService;
    private final SpeciesRepository speciesRepository;
    private final CatchPhotoRepository catchPhotoRepository;
    private final CatchConditionsRepository catchConditionsRepository;
    private final LureService lureService;
    private final CatchLureSnapshotRepository catchLureSnapshotRepository;

    public CatchController(CatchService catchService,
                           SpeciesRepository speciesRepository,
                           CatchPhotoRepository catchPhotoRepository,
                           CatchConditionsRepository catchConditionsRepository,
                           LureService lureService,
                           CatchLureSnapshotRepository catchLureSnapshotRepository) {
        this.catchService = catchService;
        this.speciesRepository = speciesRepository;
        this.catchPhotoRepository = catchPhotoRepository;
        this.catchConditionsRepository = catchConditionsRepository;
        this.lureService = lureService;
        this.catchLureSnapshotRepository = catchLureSnapshotRepository;
    }

    @ModelAttribute("lureOptions")
    public List<Lure> lureOptions() {
        return lureService.findTackleBox(CurrentUser.DEV_USER_ID);
    }

    @ModelAttribute("tideStates")
    public TideState[] tideStates() {
        return TideState.values();
    }

    @ModelAttribute("skyConditions")
    public SkyCondition[] skyConditions() {
        return SkyCondition.values();
    }

    /**
     * Available to every view rendered by this controller, so the species
     * dropdown is still populated when the form is redisplayed with errors.
     */
    @ModelAttribute("speciesOptions")
    public List<Species> speciesOptions() {
        return speciesRepository.findByActiveTrueOrderByCommonName();
    }

    @GetMapping("/catches")
    public String list(Model model) {
        model.addAttribute("catches", catchService.findJournal(CurrentUser.DEV_USER_ID));
        return "catches/list";
    }

    @GetMapping("/catches/new")
    public String newForm(Model model) {
        model.addAttribute("catchForm", new CatchForm());
        return "catches/new";
    }

    @PostMapping("/catches")
    public String create(@Valid @ModelAttribute("catchForm") CatchForm catchForm,
                         BindingResult bindingResult) {

        // Some rules span two fields and do not fit a single-field annotation.
        // This mirrors the database constraint requiring both or neither.
        boolean hasLatitude = catchForm.getLatitude() != null;
        boolean hasLongitude = catchForm.getLongitude() != null;
        if (hasLatitude != hasLongitude) {
            bindingResult.rejectValue("longitude", "location.incomplete",
                    "Enter both latitude and longitude, or leave both blank");
        }

        if (bindingResult.hasErrors()) {
            return "catches/new";
        }

        try {
            Catch saved = catchService.create(CurrentUser.DEV_USER_ID, catchForm);
            return "redirect:/catches/" + saved.getId();
        } catch (PhotoUploadException e) {
            // Turns a rejected photo into a field error so the rest of the
            // user's input survives, rather than failing the whole request.
            bindingResult.rejectValue("photo", "photo.invalid", e.getMessage());
            return "catches/new";
        }
    }

    @GetMapping("/catches/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("catchRecord", catchService.findOwned(id, CurrentUser.DEV_USER_ID));
        model.addAttribute("hasPhoto",
                catchPhotoRepository.findFirstByCatchRecordIdOrderByIdAsc(id).isPresent());
        model.addAttribute("conditions",
                catchConditionsRepository.findById(id).orElse(null));
        model.addAttribute("lureSnapshot",
                catchLureSnapshotRepository.findById(id).orElse(null));
        return "catches/detail";
    }

    /**
     * Fires when the upload exceeds the configured limit. The request is aborted
     * mid-stream, so unlike other validation errors the user's other fields are
     * genuinely gone; the best we can do is say why.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String photoTooLarge(Model model) {
        model.addAttribute("catchForm", new CatchForm());
        model.addAttribute("uploadTooLarge", true);
        return "catches/new";
    }
}
