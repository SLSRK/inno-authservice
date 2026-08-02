package com.innowise.authservice.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponseDto(

        Long id,

        String name,

        String surname,

        LocalDate birthDate,

        Boolean active,

        String email,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
