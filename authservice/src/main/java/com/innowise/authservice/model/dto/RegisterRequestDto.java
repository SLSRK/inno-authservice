package com.innowise.authservice.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(

        @NotBlank(message = "Login cannot be empty")
        String login,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password cannot be shorter 8 characters")
        String password,

        @NotBlank(message = "Role cannot be empty")
        @Pattern(
                regexp = "USER|ADMIN",
                message = "Role must be USER or ADMIN"
        )
        String role,

        @Valid
        UserRequestDto userRequestDto
) {
}
