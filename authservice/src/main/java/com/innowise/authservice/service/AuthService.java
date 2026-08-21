package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.AuthResponseDto;
import com.innowise.authservice.model.dto.LoginRequestDto;
import com.innowise.authservice.model.dto.RegisterRequestDto;
import com.innowise.authservice.model.dto.ValidateRequestDto;
import com.innowise.authservice.model.dto.ValidateResponseDto;

public interface AuthService {
    /**
     * Creates and saves user credentials;
     * @param registerRequestDto data of the user to create.
     */
    void register(RegisterRequestDto registerRequestDto);

    /**
     * Logs in if login and password pair is valid;
     * @param loginRequestDto login and password;
     * @return returns access token and refresh token.
     */
    AuthResponseDto login(LoginRequestDto loginRequestDto);

    /**
     * Updates access token by refresh token;
     * @param refreshToken refresh token;
     * @return returns new access token.
     */
    AuthResponseDto refresh(String refreshToken);

    /**
     * Checks access token for validity;
     * @param validateRequestDto request body with token;
     * @return returns access token is valid or not.
     */
    ValidateResponseDto validate(ValidateRequestDto validateRequestDto);
}
