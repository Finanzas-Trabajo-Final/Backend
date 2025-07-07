package com.grupo4.americash.controller;

import com.grupo4.americash.dto.UserDto;
import com.grupo4.americash.entity.User;
import com.grupo4.americash.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "UserAccount", description = "Endpoints for user account management")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping("getbyIdentifier/{identifier}")
    public HttpEntity<UserDto>getUser(@PathVariable String identifier) {
        Optional<User> user = userService.findByIdentifier(identifier);
        return user.map(v-> ResponseEntity.ok(UserDto.fromUser(v))).orElseGet(()->ResponseEntity.notFound().build());
    }
    @GetMapping("getbyEmail/{email}")
    public HttpEntity<UserDto> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.map(v -> ResponseEntity.ok(UserDto.fromUser(v)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
