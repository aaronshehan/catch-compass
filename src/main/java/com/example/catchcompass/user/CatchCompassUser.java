package com.example.catchcompass.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * The signed-in angler, as Spring Security sees them.
 *
 * <p>Carries the database id, which is the whole point: every query in this
 * application is scoped by user id, and this is what replaces the hardcoded
 * CurrentUser.DEV_USER_ID that stood in for it until now.
 *
 * <p>Controllers receive it with {@code @AuthenticationPrincipal}.
 */
public class CatchCompassUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;

    public CatchCompassUser(Long id, String username, String passwordHash, boolean enabled) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
    }

    public static CatchCompassUser from(User user) {
        return new CatchCompassUser(
                user.getId(), user.getUsername(), user.getPasswordHash(), user.isEnabled());
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Every angler has the same rights over their own data, so there are no
        // roles to distinguish. Ownership, not authority, is what this app checks.
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
