package com.kpaatmik.weatherapplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kpaatmik.weatherapplication.entity.City;

public interface CityRepository extends JpaRepository<City, Long> {
	boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);

	Optional<City> findByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);

	List<City> findAllByOrderByNameAsc();

	List<City> findAllByActiveTrueOrderByNameAsc();
}
