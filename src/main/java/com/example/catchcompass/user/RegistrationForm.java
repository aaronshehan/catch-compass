package com.example.catchcompass.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Choose a username")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "[A-Za-z0-9._-]+",
            message = "Username can contain letters, numbers, dots, dashes and underscores")
    private String username;

    /**
     * Length is the rule that actually matters. Composition requirements
     * ("one capital, one symbol") push people towards Passw0rd! and are no
     * longer recommended practice.
     */
    @NotBlank(message = "Choose a password")
    @Size(min = 12, max = 200, message = "Password must be at least 12 characters")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
