package com.example.catchcompass.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AccountView> register(@Valid @RequestBody RegistrationForm form) {
        User created = userService.register(form);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AccountView(created.getId(), created.getUsername(),
                        created.getUnitPreference().name()));
    }

    /**
     * Authenticates and writes the result into the session.
     *
     * <p>Spring Security's built-in form login expects an HTML form post and a
     * redirect. A JSON client wants neither, so authentication is done
     * explicitly here: verify the credentials, then persist the security
     * context so the session cookie carries the identity on later requests.
     */
    @PostMapping("/login")
    public AccountView login(@Valid @RequestBody Credentials credentials,
                             HttpServletRequest request,
                             HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        credentials.username(), credentials.password()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        CatchCompassUser user = (CatchCompassUser) authentication.getPrincipal();
        return new AccountView(user.getId(), user.getUsername(), null);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /**
     * Who am I? The frontend calls this on load to decide between showing the
     * app and showing the login screen. 401 when signed out is the answer, not
     * an error.
     */
    @GetMapping("/me")
    public ResponseEntity<AccountView> me(@AuthenticationPrincipal CatchCompassUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new AccountView(user.getId(), user.getUsername(), null));
    }

    /**
     * Deliberately says nothing about which half was wrong. "No such user" tells
     * an attacker which usernames exist.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemBody> onBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ProblemBody("Incorrect username or password"));
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ProblemBody> onUsernameTaken(UsernameTakenException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ProblemBody(e.getMessage()));
    }

    public record Credentials(@NotBlank String username, @NotBlank String password) {
    }

    public record AccountView(Long id, String username, String unitPreference) {
    }

    public record ProblemBody(String detail) {
    }
}
