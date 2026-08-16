package com.example.catchcompass.catchlog;

/**
 * A rejected photo upload. Carries a message safe to show the user, so the
 * form can redisplay it as a field error rather than failing the whole request.
 */
public class PhotoUploadException extends RuntimeException {

    public PhotoUploadException(String message) {
        super(message);
    }
}
