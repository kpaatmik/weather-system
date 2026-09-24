package com.kpaatmik.weather_application.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.kpaatmik.weather_application.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtProperties jwtProperties;

	private SecretKey getSigningKey() {

		return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(String username, String role) {

		Date now = new Date();

		Date expiration = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

		return Jwts.builder().subject(username).claim("role", role).issuedAt(now).expiration(expiration)
				.signWith(getSigningKey()).compact();
	}

	public String generateRefreshToken(String username) {

		Date now = new Date();

		Date expiration = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

		return Jwts.builder().subject(username).issuedAt(now).expiration(expiration).claim("type", "refresh")
				.signWith(getSigningKey()).compact();
	}

	public String extractUsername(String token) {

		return extractAllClaims(token).getSubject();
	}

	public String extractRole(String token) {

		return extractAllClaims(token).get("role", String.class);
	}

	public boolean isTokenValid(String token, String username) {

		try {

			String extractedUsername = extractUsername(token);

			return extractedUsername.equals(username) && !isTokenExpired(token);

		} catch (Exception e) {

			return false;
		}
	}

	public boolean isRefreshToken(String token) {

		try {

			String type = extractAllClaims(token).get("type", String.class);

			return "refresh".equals(type);

		} catch (Exception e) {

			return false;
		}
	}

	private boolean isTokenExpired(String token) {

		return extractAllClaims(token).getExpiration().before(new Date());
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}
}