package com.innowise.authservice.client;

import com.innowise.authservice.model.dto.UserRequestDto;
import com.innowise.authservice.model.dto.UserResponseDto;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserClient {

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;
    private final JwtService jwtService;

    public UserResponseDto createUserInUserService(UserRequestDto userRequestDto) {
        String url = userServiceUrl + "/api/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtService.createAccessToken(1L, Role.ADMIN));
        HttpEntity<UserRequestDto> request = new HttpEntity<>(userRequestDto, headers);

        return restTemplate.postForObject(
                url,
                request,
                UserResponseDto.class);
    }
}
