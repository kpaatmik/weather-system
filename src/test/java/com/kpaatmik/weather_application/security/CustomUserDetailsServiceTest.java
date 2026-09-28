package com.kpaatmik.weather_application.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.kpaatmik.weather_application.entity.Role;
import com.kpaatmik.weather_application.entity.User;
import com.kpaatmik.weather_application.repository.UserRepository;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);

        userDetailsService =
                new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {

        User user = User.builder()
                .username("aatmik")
                .password("encoded-password")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findByUsername("aatmik"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("aatmik");

        assertEquals("aatmik", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );
    }

    @Test
    void loadUserByUsername_shouldDisableInactiveUser() {

        User user = User.builder()
                .username("aatmik")
                .password("encoded-password")
                .role(Role.USER)
                .active(false)
                .build();

        when(userRepository.findByUsername("aatmik"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("aatmik");

        assertFalse(result.isEnabled());
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findByUsername("aatmik"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("aatmik")
        );
    }
}