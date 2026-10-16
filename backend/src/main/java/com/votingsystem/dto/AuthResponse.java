package com.votingsystem.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) { }
