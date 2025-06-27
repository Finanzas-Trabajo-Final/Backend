package com.grupo4.americash.service;

public interface JwtService {

    String generateToken(String username);
    String extractUsername(String token);

    boolean isTokenExpired(String token);

    boolean isTokenValid(String token, String username);
}
