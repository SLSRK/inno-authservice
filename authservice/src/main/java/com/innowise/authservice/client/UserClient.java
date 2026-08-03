package com.innowise.authservice.client;

import com.innowise.authservice.model.dto.UserRequestDto;
import com.innowise.authservice.model.dto.UserResponseDto;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public UserResponseDto createUserInUserService(UserRequestDto userRequestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtService.createAccessToken(1L, Role.ADMIN));
        HttpEntity<UserRequestDto> request = new HttpEntity<>(userRequestDto, headers);

        return restTemplate.postForObject(
                "/api/users",
                request,
                UserResponseDto.class);
    }

    public void deleteUserInUserService(Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtService.createAccessToken(1L, Role.ADMIN));
        HttpEntity<UserRequestDto> request = new HttpEntity<>(headers);

        restTemplate.exchange(
                "/api/users/{id}",
                HttpMethod.DELETE,
                request,
                Void.class,
                id
        );
    }
}
