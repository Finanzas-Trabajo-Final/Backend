package com.grupo4.americash.service;

import com.grupo4.americash.entity.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByIdentifier(String identifier);
}
