package com.grupo4.americash.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Authentication response that returns a JWT token")
public class AuthResponse {

    @Schema(description = "JWT token generated after authentication")
    private String token;

    @Schema(description = "User identifier, can be id, email, or username", example = "john.doe")
    private String userId;
}
