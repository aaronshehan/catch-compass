package com.example.catchcompass.catchlog;

import com.example.catchcompass.shared.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CatchController {

    private final CatchService catchService;

    public CatchController(CatchService catchService) {
        this.catchService = catchService;
    }

    @GetMapping("/catches")
    public String list(Model model) {
        model.addAttribute("catches", catchService.findJournal(CurrentUser.DEV_USER_ID));
        return "catches/list";
    }

    @GetMapping("/catches/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("catchRecord", catchService.findOwned(id, CurrentUser.DEV_USER_ID));
        return "catches/detail";
    }
}
