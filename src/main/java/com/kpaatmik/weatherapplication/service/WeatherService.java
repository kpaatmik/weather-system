package com.kpaatmik.weatherapplication.service;

import org.springframework.stereotype.Service;

import com.kpaatmik.weatherapplication.audit.AuditAction;
import com.kpaatmik.weatherapplication.audit.AuditEntityType;
import com.kpaatmik.weatherapplication.dto.response.WeatherResponse;
import com.kpaatmik.weatherapplication.security.SecurityUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class WeatherService {

	private final AuditService auditService;
	private final UserService userService;
	private final WeatherCacheService weatherCacheService;

	public WeatherResponse getWeather(Long cityId) {
		log.info("Weather request received: cityId={}", cityId);
		auditService.log(userService.getUserId(SecurityUtil.getCurrentUsername()), AuditAction.WEATHER_SEARCHED,
				AuditEntityType.WEATHER, null, "Weather searched: " + cityId);
		return weatherCacheService.getWeather(cityId);
	}
}