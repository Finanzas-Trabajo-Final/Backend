package com.grupo4.americash.service;

import com.grupo4.americash.dto.AuthRequest;
import com.grupo4.americash.dto.AuthResponse;
import com.grupo4.americash.dto.RegisterRequest;
import jakarta.transaction.Transactional;

public interface AuthService {

    @Transactional
    AuthResponse register(RegisterRequest request);

    @Transactional
    AuthResponse authenticate(AuthRequest request);

    @Transactional
    void deleteUser(String dni);
}
