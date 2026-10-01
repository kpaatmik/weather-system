package com.kpaatmik.weatherapplication.service;

import org.springframework.stereotype.Service;

import com.kpaatmik.weatherapplication.entity.User;
import com.kpaatmik.weatherapplication.repository.UserRepository;
import com.kpaatmik.weatherapplication.security.SecurityUtil;

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
