package com.kpaatmik.weather_application.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cities", uniqueConstraints = {
		@UniqueConstraint(name = "uk_city_name_country", columnNames = { "name", "country" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 100)
	private String state;

	@Column(nullable = false, length = 10)
	private String country;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;
	@Builder.Default
	@Column(nullable = false)
	private Boolean active = true;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {

		LocalDateTime now = LocalDateTime.now();

		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {

		updatedAt = LocalDateTime.now();
	}

}