package com.example.catchcompass.shared;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Placeholder for the dashboard described in the README. Until that exists,
     * sending people straight to the journal beats a 404 on the front door.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/catches";
    }
}
