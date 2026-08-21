package com.innowise.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateRequestDto(
        @NotBlank
        String token
) {
}
