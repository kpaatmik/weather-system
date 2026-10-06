package com.kpaatmik.weatherapplication.dto.response;

public record OAuthTokenResponse(String access_token, String token_type, long expires_in) {
}