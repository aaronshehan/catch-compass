package com.example.catchcompass.catchlog;

import com.example.catchcompass.shared.CurrentUser;
import com.example.catchcompass.species.Species;
import com.example.catchcompass.species.SpeciesRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class CatchController {

    private final CatchService catchService;
    private final SpeciesRepository speciesRepository;

    public CatchController(CatchService catchService, SpeciesRepository speciesRepository) {
        this.catchService = catchService;
        this.speciesRepository = speciesRepository;
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

        Catch saved = catchService.create(CurrentUser.DEV_USER_ID, catchForm);
        return "redirect:/catches/" + saved.getId();
    }

    @GetMapping("/catches/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("catchRecord", catchService.findOwned(id, CurrentUser.DEV_USER_ID));
        return "catches/detail";
    }
}
