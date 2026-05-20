package com.sgr9.url_shortener.service;

import com.sgr9.url_shortener.dto.LoginRequest;
import com.sgr9.url_shortener.models.User;
import com.sgr9.url_shortener.repository.UserRepository;
import com.sgr9.url_shortener.security.jwt.JwtAuthenticationResponse;
import com.sgr9.url_shortener.security.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @Test
    void authenticateUserUsesTrimmedIdentifierAndReturnsToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setIdentifier(" sgr9@example.com ");
        loginRequest.setPassword("password");
        UserDetailsImpl principal = new UserDetailsImpl(1L, "sgr9", "sgr9@example.com", "encoded-password", List.of());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtUtils.generateToken(principal)).thenReturn("jwt-token");

        JwtAuthenticationResponse response = userService.authenticateUser(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getPrincipal()).isEqualTo("sgr9@example.com");
        assertThat(tokenCaptor.getValue().getCredentials()).isEqualTo("password");
    }

    @Test
    void registerUserNormalizesEmailAndEncodesPassword() {
        User user = user(" sgr9 ", " SGR9@EXAMPLE.COM ", "password");
        when(userRepository.existsByUsername("sgr9")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("sgr9@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.registerUser(user);

        assertThat(savedUser.getUsername()).isEqualTo("sgr9");
        assertThat(savedUser.getEmail()).isEqualTo("sgr9@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        verify(userRepository).save(user);
    }

    @Test
    void registerUserRejectsDuplicateUsername() {
        User user = user("sgr9", "sgr9@example.com", "password");
        when(userRepository.existsByUsername("sgr9")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is already taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserRejectsDuplicateEmailIgnoringCase() {
        User user = user("sgr9", "SGR9@EXAMPLE.COM", "password");
        when(userRepository.existsByUsername("sgr9")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("sgr9@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use");
        verify(userRepository, never()).save(any());
    }

    private User user(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("ROLE_USER");
        return user;
    }
}
