package com.kpaatmik.weatherapplication.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kpaatmik.weatherapplication.security.SecurityUtil;

class SecurityUtilTest {

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getCurrentUsername_shouldReturnUsername() {

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("aatmik", null,
				List.of());

		SecurityContextHolder.getContext().setAuthentication(authentication);

		assertEquals("aatmik", SecurityUtil.getCurrentUsername());
	}

	@Test
	void getCurrentUsername_shouldReturnNullWhenNotAuthenticated() {

		SecurityContextHolder.clearContext();

		assertNull(SecurityUtil.getCurrentUsername());
	}

	@Test
	void getCurrentUsername_shouldReturnNullWhenAuthenticationIsNotAuthenticated() {

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("aatmik", null);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		assertNull(SecurityUtil.getCurrentUsername());
	}
}