package com.kpaatmik.weather_application.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.kpaatmik.weather_application.entity.*;
import com.kpaatmik.weather_application.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks CustomUserDetailsService service;

    @Test
    void loadUserByUsername_shouldMapUserAndRole() {
        User user = User.builder().id(1L).username("admin")
                .password("HASH").role(Role.ADMIN).active(true).build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("admin");

        assertEquals("admin", details.getUsername());
        assertEquals("HASH", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_shouldDisableInactiveUser() {
        User user = User.builder().username("user")
                .password("HASH").role(Role.USER).active(false).build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("user");

        assertFalse(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_shouldThrowForUnknownUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing"));
    }
}
