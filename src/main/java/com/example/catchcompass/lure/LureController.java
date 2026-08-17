package com.example.catchcompass.lure;

import com.example.catchcompass.shared.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LureController {

    private final LureService lureService;

    public LureController(LureService lureService) {
        this.lureService = lureService;
    }

    @ModelAttribute("lureTypes")
    public LureType[] lureTypes() {
        return LureType.values();
    }

    @ModelAttribute("presentations")
    public LurePresentation[] presentations() {
        return LurePresentation.values();
    }

    @GetMapping("/lures")
    public String list(Model model) {
        model.addAttribute("lures", lureService.findTackleBox(CurrentUser.DEV_USER_ID));
        return "lures/list";
    }

    @GetMapping("/lures/new")
    public String newForm(Model model) {
        model.addAttribute("lureForm", new LureForm());
        return "lures/new";
    }

    @PostMapping("/lures")
    public String create(@Valid @ModelAttribute("lureForm") LureForm lureForm,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "lures/new";
        }
        lureService.create(CurrentUser.DEV_USER_ID, lureForm);
        return "redirect:/lures";
    }
}
