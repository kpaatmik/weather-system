package com.kpaatmik.weather_application.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.kpaatmik.weather_application.entity.*;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired CityRepository cityRepository;
    @Autowired AuditLogRepository auditLogRepository;

    @Test
    void userRepository_shouldPersistAndFindByUsernameAndEmail() {
        User user = User.builder()
                .username("aatmik")
                .email("aatmik@example.com")
                .password("HASH")
                .role(Role.USER)
                .active(true)
                .build();

        userRepository.saveAndFlush(user);

        assertTrue(userRepository.existsByUsername("aatmik"));
        assertTrue(userRepository.existsByEmail("aatmik@example.com"));
        assertEquals("aatmik",
                userRepository.findByUsername("aatmik").orElseThrow().getUsername());
        assertEquals("aatmik@example.com",
                userRepository.findByEmail("aatmik@example.com").orElseThrow().getEmail());
    }

    @Test
    void userRepository_shouldEnforceUniqueUsernameAndEmail() {
        userRepository.saveAndFlush(User.builder()
                .username("aatmik").email("a@example.com").password("HASH")
                .role(Role.USER).active(true).build());

        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(User.builder()
                    .username("aatmik").email("b@example.com").password("HASH")
                    .role(Role.USER).active(true).build());
        });
    }

    @Test
    void cityRepository_shouldFindCaseInsensitiveNameAndCountry() {
        cityRepository.saveAndFlush(City.builder()
                .name("KANNUR").country("IN")
                .latitude(11.8745).longitude(75.3704).active(true).build());

        assertTrue(cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase("kannur", "in"));
        assertTrue(cityRepository.findByNameIgnoreCaseAndCountryIgnoreCase("kannur", "in").isPresent());
    }

    @Test
    void cityRepository_shouldReturnCitiesOrderedByName() {
        cityRepository.saveAllAndFlush(List.of(
                City.builder().name("Z CITY").country("IN").latitude(1.0).longitude(1.0).active(true).build(),
                City.builder().name("A CITY").country("IN").latitude(2.0).longitude(2.0).active(true).build()
        ));

        List<City> cities = cityRepository.findAllByOrderByNameAsc();

        assertEquals("A CITY", cities.get(0).getName());
        assertEquals("Z CITY", cities.get(1).getName());
    }

    @Test
    void cityRepository_shouldReturnOnlyActiveCities() {
        cityRepository.saveAllAndFlush(List.of(
                City.builder().name("ACTIVE").country("IN").latitude(1.0).longitude(1.0).active(true).build(),
                City.builder().name("INACTIVE").country("IN").latitude(2.0).longitude(2.0).active(false).build()
        ));

        List<City> cities = cityRepository.findAllByActiveTrueOrderByNameAsc();

        assertEquals(1, cities.size());
        assertEquals("ACTIVE", cities.get(0).getName());
    }

    @Test
    void auditRepository_shouldPersistAuditWithoutUser() {
        AuditLog log = AuditLog.builder()
                .action("USER_REGISTERED")
                .entityType("USER")
                .entityId(1L)
                .details("registered")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        AuditLog saved = auditLogRepository.saveAndFlush(log);

        assertNotNull(saved.getId());
        assertEquals("USER_REGISTERED",
                auditLogRepository.findById(saved.getId()).orElseThrow().getAction());
    }
}
