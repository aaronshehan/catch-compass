package com.example.catchcompass.shared;

/**
 * Placeholder for the signed-in user until Spring Security arrives in Phase 4.
 *
 * <p>Every query is already scoped by user id, so replacing this with the real
 * authenticated principal should not require changing any query or template.
 */
public final class CurrentUser {

    public static final Long DEV_USER_ID = 1L;

    private CurrentUser() {
    }
}
