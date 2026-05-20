package com.sgr9.url_shortener.service;


import com.sgr9.url_shortener.models.User;
import com.sgr9.url_shortener.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    private static final String GENERIC_AUTH_ERROR = "Invalid username/email or password";
    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        User user = (isEmail(normalizedIdentifier)
                ? userRepository.findByEmailIgnoreCase(normalizedIdentifier.toLowerCase())
                : userRepository.findByUsername(normalizedIdentifier))
                .orElseThrow(() -> new UsernameNotFoundException(GENERIC_AUTH_ERROR));
        return UserDetailsImpl.build(user);
    }

    private boolean isEmail(String identifier) {
        return identifier.matches(EMAIL_PATTERN);
    }
}
