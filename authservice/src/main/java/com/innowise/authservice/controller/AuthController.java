package com.innowise.authservice.controller;

import com.innowise.authservice.model.dto.AuthResponseDto;
import com.innowise.authservice.model.dto.LoginRequestDto;
import com.innowise.authservice.model.dto.RefreshRequestDto;
import com.innowise.authservice.model.dto.RegisterRequestDto;
import com.innowise.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        authService.register(registerRequestDto);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return authService.login(loginRequestDto);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponseDto refresh(@Valid @RequestBody RefreshRequestDto refreshRequestDto) {
        return authService.refresh(refreshRequestDto.refreshToken());
    }
}
