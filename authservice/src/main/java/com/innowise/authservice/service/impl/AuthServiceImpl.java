package com.innowise.authservice.service.impl;

import com.innowise.authservice.client.UserClient;
import com.innowise.authservice.exception.NotFoundException;
import com.innowise.authservice.exception.LoginException;
import com.innowise.authservice.exception.RegistrationException;
import com.innowise.authservice.model.dto.AuthResponseDto;
import com.innowise.authservice.model.dto.LoginRequestDto;
import com.innowise.authservice.model.dto.RegisterRequestDto;
import com.innowise.authservice.model.dto.UserRequestDto;
import com.innowise.authservice.model.dto.UserResponseDto;
import com.innowise.authservice.model.dto.ValidateRequestDto;
import com.innowise.authservice.model.dto.ValidateResponseDto;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClient userClient;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Transactional
    public void register(RegisterRequestDto registerRequestDto) {
        log.debug("Creating a new user with login={}, role={}, email={}",
                registerRequestDto.login(),
                registerRequestDto.role(),
                registerRequestDto.userRequestDto().email());
        AuthUser user = new AuthUser();

        user.setLogin(registerRequestDto.login());
        user.setPassword(passwordEncoder.encode(registerRequestDto.password()));
        user.setRole(Role.valueOf(registerRequestDto.role()));

        UserRequestDto userRequestDto = registerRequestDto.userRequestDto();
        UserResponseDto userResponseDto;

        try {
            userResponseDto = userClient.createUserInUserService(userRequestDto);
        } catch (Exception e) {
            throw new RegistrationException("Registration failed:" + e);
        }
        user.setUserId(userResponseDto.id());
        log.debug("Created a new user profile for userservice with the id={}",
                user.getUserId());
        try {
            authUserRepository.save(user);
        } catch (Exception e) {
            userClient.deleteUserInUserService(user.getUserId());
            throw new RegistrationException("Registration failed:" + e);
        }
    }

    public AuthResponseDto login(LoginRequestDto loginRequestDto){
        AuthUser user = authUserRepository.findByLogin(loginRequestDto.login())
                .orElseThrow(() -> new LoginException("Invalid username or password"));

        if(!passwordEncoder.matches(loginRequestDto.password(), user.getPassword())){
            throw new LoginException("Invalid username or password");
        }

        String accessToken = jwtService.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtService.createRefreshToken(user.getUserId());

        return new AuthResponseDto(accessToken, refreshToken);
    }

    public AuthResponseDto refresh(String refreshToken){
        Long userId = jwtService.getUserId(refreshToken, "REFRESH");

        AuthUser user = authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String newAccessToken = jwtService.createAccessToken(userId, user.getRole());

        return new AuthResponseDto(newAccessToken, refreshToken);
    }

    public ValidateResponseDto validate(ValidateRequestDto validateRequestDto){
        try{
            jwtService.validateToken(validateRequestDto.token(), "ACCESS");
            return new ValidateResponseDto(true);
        } catch (Exception e) {
            log.info("Invalid token: {}", e.getMessage());
            return new ValidateResponseDto(false);
        }
    }
}
