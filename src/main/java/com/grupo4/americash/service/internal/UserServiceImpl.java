package com.grupo4.americash.service.internal;

import com.grupo4.americash.entity.User;
import com.grupo4.americash.repository.UserRepository;
import com.grupo4.americash.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        Optional<User> user = userRepository.findByEmail(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByUsername(identifier);
        }
        if (user.isEmpty()) {
            try {
                Long id = Long.parseLong(identifier);
                user = userRepository.findById(id);
            } catch (NumberFormatException ignored) {
            }
        }
        return user;
    }
}