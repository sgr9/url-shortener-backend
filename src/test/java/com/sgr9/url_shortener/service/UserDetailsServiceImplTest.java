package com.sgr9.url_shortener.service;

import com.sgr9.url_shortener.models.User;
import com.sgr9.url_shortener.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameFindsUserByUsername() {
        User user = user("sgr9", "sgr9@example.com");
        when(userRepository.findByUsername("sgr9")).thenReturn(Optional.of(user));

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(" sgr9 ");

        assertThat(userDetails.getUsername()).isEqualTo("sgr9");
        verify(userRepository).findByUsername("sgr9");
    }

    @Test
    void loadUserByUsernameFindsUserByEmailIgnoringCase() {
        User user = user("sgr9", "sgr9@example.com");
        when(userRepository.findByEmailIgnoreCase("sgr9@example.com")).thenReturn(Optional.of(user));

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(" SGR9@EXAMPLE.COM ");

        assertThat(userDetails.getUsername()).isEqualTo("sgr9");
        verify(userRepository).findByEmailIgnoreCase("sgr9@example.com");
    }

    @Test
    void loadUserByUsernameUsesGenericErrorForUnknownIdentifier() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid username/email or password");
    }

    private User user(String username, String email) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole("ROLE_USER");
        return user;
    }
}
