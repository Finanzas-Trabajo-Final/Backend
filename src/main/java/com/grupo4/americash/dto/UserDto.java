package com.grupo4.americash.dto;

import com.grupo4.americash.entity.User;

public record UserDto(
        Long id,
        String email,
        String username
) {

    public static UserDto fromUser(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getUsername());
    }
}
