package com.example.catchcompass.catchlog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * The lure types, served from the server so the dropdown cannot drift out of
 * sync with the CHECK constraint in V7.
 */
@RestController
public class LureTypeController {

    @GetMapping("/api/lure-types")
    public List<String> lureTypes() {
        return Arrays.stream(LureType.values()).map(Enum::name).toList();
    }
}
