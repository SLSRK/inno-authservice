package com.innowise.authservice.model.dto;

public record AuthResponseDto(

        String accessToken,

        String refreshToken
) {
}
