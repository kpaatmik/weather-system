package com.kpaatmik.weather_application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kpaatmik.weather_application.entity.City;

public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(
            String name,
            String country
    );

    Optional<City> findByNameIgnoreCaseAndCountryIgnoreCase(
            String name,
           String country
    );

    List<City> findAllByOrderByNameAsc();	
    List<City> findAllByActiveTrueOrderByNameAsc();
}
