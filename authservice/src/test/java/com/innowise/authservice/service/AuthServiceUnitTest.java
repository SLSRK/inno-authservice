package com.innowise.authservice.service;

import com.innowise.authservice.client.UserClient;
import com.innowise.authservice.exception.LoginException;
import com.innowise.authservice.model.dto.AuthResponseDto;
import com.innowise.authservice.model.dto.LoginRequestDto;
import com.innowise.authservice.model.dto.RegisterRequestDto;
import com.innowise.authservice.model.dto.UserRequestDto;
import com.innowise.authservice.model.dto.UserResponseDto;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRequestDto userRequestDto;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto(
                "Ivan",
                "Slesarenko",
                LocalDate.of(2000, 1, 1),
                "ivan@test.com"
        );
    }

    @Test
    void register_shouldHashPasswordAndSaveUser() {
        RegisterRequestDto request = new RegisterRequestDto(
                "ivan",
                "password123",
                "USER",
                userRequestDto
        );

        when(passwordEncoder.encode("password123")).thenReturn("hashed_pass");

        UserResponseDto responseDto = new UserResponseDto(
                1L,
                "Ivan",
                "Slesarenko",
                LocalDate.of(2000, 1, 1),
                true,
                "ivan@test.com",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(userClient.createUserInUserService(userRequestDto))
                .thenReturn(responseDto);

        authService.register(request);
        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        verify(authUserRepository).save(captor.capture());

        AuthUser saved = captor.getValue();

        assertThat(saved.getLogin()).isEqualTo("ivan");
        assertThat(saved.getPassword()).isEqualTo("hashed_pass");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getUserId()).isEqualTo(1L);
    }

    @Test
    void login_shouldReturnTokens_whenPasswordCorrect() {
        AuthUser user = defaultUser();

        when(authUserRepository.findByLogin("ivan"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed"))
                .thenReturn(true);
        when(jwtService.createAccessToken(1L, Role.USER))
                .thenReturn("access");
        when(jwtService.createRefreshToken(1L))
                .thenReturn("refresh");

        AuthResponseDto response = authService.login(
                new LoginRequestDto("ivan", "password123")
        );
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void login_shouldThrow_whenPasswordInvalid() {
        AuthUser user = defaultUser();

        when(authUserRepository.findByLogin("ivan"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed"))
                .thenReturn(false);
        assertThatThrownBy(() ->
                authService.login(new LoginRequestDto("ivan", "wrong"))
        ).isInstanceOf(LoginException.class);
    }

    @Test
    void refresh_shouldReturnNewAccessToken() {
        AuthUser user = defaultUser();

        when(jwtService.getUserId("refreshToken"))
                .thenReturn(1L);
        when(authUserRepository.findByUserId(1L))
                .thenReturn(Optional.of(user));
        when(jwtService.createAccessToken(1L, Role.USER))
                .thenReturn("newAccess");

        AuthResponseDto response = authService.refresh("refreshToken");
        assertThat(response.accessToken()).isEqualTo("newAccess");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
    }

    private AuthUser defaultUser(){
        AuthUser authUser = new AuthUser();
        authUser.setUserId(1L);
        authUser.setLogin("ivan");
        authUser.setPassword("hashed");
        authUser.setRole(Role.USER);
        authUser.setUserId(1L);

        return authUser;
    }
}