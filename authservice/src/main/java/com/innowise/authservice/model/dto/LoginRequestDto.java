package com.innowise.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(

        @NotBlank(message = "Login cannot be empty")
        String login,

        @NotBlank(message = "Password cannot be empty")
        String password
) {
}
