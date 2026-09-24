package com.kpaatmik.weather_application.service;

import org.springframework.stereotype.Service;

import com.kpaatmik.weather_application.entity.User;
import com.kpaatmik.weather_application.repository.UserRepository;
import com.kpaatmik.weather_application.security.SecurityUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public Long getUserId(String userName) {
		String currentUserName = SecurityUtil.getCurrentUsername();
		User currentUser = userRepository.findByUsername(currentUserName).orElse(null);
		if ((currentUser != null)) {
			return currentUser.getId();
		}

		return null;
	}
}
