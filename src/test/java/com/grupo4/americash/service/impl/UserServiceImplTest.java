package com.grupo4.americash.service.impl;

import com.grupo4.americash.entity.User;
import com.grupo4.americash.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void findByUsername_shouldReturnUser_whenExists() {
        String username = "testUser";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        String email = "user@example.com";
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        Long id = 1L;
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(id);

        assertTrue(result.isPresent());
    }

    @Test
    void findByIdentifier_shouldReturnUser_whenEmailMatches() {
        String identifier = "user@example.com";
        User user = new User();
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByIdentifier(identifier);

        assertTrue(result.isPresent());
        verify(userRepository).findByEmail(identifier);
    }

    @Test
    void findByIdentifier_shouldReturnUser_whenIdMatches() {
        String identifier = "42";
        User user = new User();
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByIdentifier(identifier);

        assertTrue(result.isPresent());
        verify(userRepository).findById(42L);
    }
}
