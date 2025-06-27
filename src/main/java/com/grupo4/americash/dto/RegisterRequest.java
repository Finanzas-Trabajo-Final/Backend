package com.grupo4.americash.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Data for registering a new user")
public class RegisterRequest {

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "User's email address",  example = "johndoe@example.com")
    private String email;

    @Schema(description = "User's password", example = "123")
    private String password;
}
