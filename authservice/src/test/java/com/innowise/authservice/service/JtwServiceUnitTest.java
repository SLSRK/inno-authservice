package com.innowise.authservice.service;

import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JtwServiceUnitTest {
    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = "jwt-secret-for-test-JzdWIiOiI1Iiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODU3NTM1MjAsImV4cC";
        jwtService = new JwtServiceImpl(secret, 15, 60);
    }

    @Test
    void shouldCreateAndValidateAccessToken() {
        String token = jwtService.createAccessToken(1L, Role.USER);

        assertThat(token).isNotBlank();

        var claims = jwtService.validateToken(token, "ACCESS");

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void shouldCreateAndValidateRefreshToken() {
        String token = jwtService.createRefreshToken(5L);

        assertThat(token).isNotBlank();

        var claims = jwtService.validateToken(token, "REFRESH");

        assertThat(claims.getSubject()).isEqualTo("5");
    }

    @Test
    void shouldGetUserId() {
        String token = jwtService.createAccessToken(42L, Role.ADMIN);

        Long userId = jwtService.getUserId(token, "ACCESS");

        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void shouldFailOnInvalidToken() {
        assertThatThrownBy(() ->
                jwtService.validateToken("invalid.token", "REFRESH")
        ).isInstanceOf(Exception.class);
    }

}
