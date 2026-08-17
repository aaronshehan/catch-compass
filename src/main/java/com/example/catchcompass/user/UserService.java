package com.example.catchcompass.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(normalise(username))
                .map(CatchCompassUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("No such user"));
    }

    @Transactional
    public User register(RegistrationForm form) {
        String username = normalise(form.getUsername());

        if (userRepository.existsByUsername(username)) {
            throw new UsernameTakenException();
        }

        // The plain password exists only inside this method and is never
        // stored, logged, or returned.
        return userRepository.save(new User(username, passwordEncoder.encode(form.getPassword())));
    }

    /**
     * Usernames are compared and stored lowercase, so "Aaron" and "aaron" are
     * the same account rather than two. The database CHECK enforces the same rule.
     */
    private String normalise(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }
}
