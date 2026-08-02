package com.innowise.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(

        @NotBlank(message = "Token cannot be empty")
        String refreshToken
) {
}
