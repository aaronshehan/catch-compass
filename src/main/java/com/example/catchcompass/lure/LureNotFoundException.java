package com.example.catchcompass.lure;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a lure does not exist <em>or</em> belongs to another user, so the
 * response never confirms that someone else's tackle exists.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LureNotFoundException extends RuntimeException {

    public LureNotFoundException(Long id) {
        super("No lure with id " + id + " is available to this user");
    }
}
