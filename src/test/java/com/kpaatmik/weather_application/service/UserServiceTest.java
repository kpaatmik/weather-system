package com.kpaatmik.weather_application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kpaatmik.weather_application.entity.User;
import com.kpaatmik.weather_application.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks UserService userService;

    @Test
    void getUserId_shouldReturnUserId() {
        User user = User.builder().id(10L).username("aatmik").build();
        when(userRepository.findByUsername("aatmik")).thenReturn(Optional.of(user));

        // The production method obtains the username from SecurityContext, not this argument.
        try (MockedStatic<com.kpaatmik.weather_application.security.SecurityUtil> mocked =
                     mockStatic(com.kpaatmik.weather_application.security.SecurityUtil.class)) {
            mocked.when(com.kpaatmik.weather_application.security.SecurityUtil::getCurrentUsername)
                    .thenReturn("aatmik");

            assertEquals(10L, userService.getUserId("ignored"));
        }
    }

    @Test
    void getUserId_shouldReturnNullWhenUserDoesNotExist() {
        when(userRepository.findByUsername("aatmik")).thenReturn(Optional.empty());

        try (MockedStatic<com.kpaatmik.weather_application.security.SecurityUtil> mocked =
                     mockStatic(com.kpaatmik.weather_application.security.SecurityUtil.class)) {
            mocked.when(com.kpaatmik.weather_application.security.SecurityUtil::getCurrentUsername)
                    .thenReturn("aatmik");

            assertNull(userService.getUserId("aatmik"));
        }
    }
}
