package com.example.catchcompass.shared;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Display formatting for templates, reachable as {@code ${@formats.dateTime(x)}}.
 *
 * <p>Thymeleaf's #temporals cannot format an Instant, because an Instant has no
 * date or time until you choose a zone to view it in. That choice is made here.
 *
 * <p>The zone is the server's, which is the same simplification the rest of the
 * application currently makes. It becomes wrong as soon as there are users in
 * more than one timezone, and is the natural thing to fix alongside the unit
 * preferences in the README.
 */
@Component("formats")
public class Formats {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d MMM yyyy");

    public String dateTime(Instant instant) {
        return instant == null ? "Not recorded" : DATE_TIME.format(instant.atZone(zone()));
    }

    public String date(Instant instant) {
        return instant == null ? "Not recorded" : DATE.format(instant.atZone(zone()));
    }

    private ZoneId zone() {
        return ZoneId.systemDefault();
    }
}
