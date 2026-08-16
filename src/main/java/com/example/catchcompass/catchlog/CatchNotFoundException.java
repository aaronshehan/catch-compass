package com.example.catchcompass.catchlog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a catch does not exist <em>or</em> belongs to another user.
 *
 * <p>Both cases deliberately produce the same 404 response so the application
 * never reveals that another user's record exists.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CatchNotFoundException extends RuntimeException {

    public CatchNotFoundException(Long id) {
        super("No catch with id " + id + " is available to this user");
    }
}
