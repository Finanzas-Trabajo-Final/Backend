package com.grupo4.americash.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Data for user authentication")
public class AuthRequest {

    @Schema(description = "User's email", example = "johndoe@example.com")
    @Email(message = "A valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "User's password", example = "123")
    @NotBlank(message = "Password is required")
    private String password;
}

