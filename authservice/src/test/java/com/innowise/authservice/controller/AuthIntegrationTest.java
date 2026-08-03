package com.innowise.authservice.controller;

import com.innowise.authservice.AuthserviceApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.innowise.authservice.client.UserClient;
import com.innowise.authservice.model.dto.AuthResponseDto;
import com.innowise.authservice.model.dto.LoginRequestDto;
import com.innowise.authservice.model.dto.RefreshRequestDto;
import com.innowise.authservice.model.dto.RegisterRequestDto;
import com.innowise.authservice.model.dto.UserRequestDto;
import com.innowise.authservice.model.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(
        classes = AuthserviceApplication.class,
        properties = {
                "jwt.secret=jwt-secret-for-test-JzdWIiOiI1Iiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODU3NTM1MjAsImV4cC",
                "access.expiration.mins=15",
                "refresh.expiration.mins=60"
        })
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    private static final java.util.concurrent.atomic.AtomicLong USER_ID_SEQUENCE = new java.util.concurrent.atomic.AtomicLong(1);
    private static final String PASSWORD = "u7mC8Ncvjkb3d7U";
    private static final String ROLE = "USER";
    private static final String NAME = "Ivan";
    private static final String SURNAME = "Slesarenko";
    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDateTime CURRENT_DATE = LocalDateTime.now();
    private static final String TEST_EMAIL_DOMAIN = "@test.com";

    @MockitoBean
    private UserClient userClient;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("authservice")
                .withUsername("postgres")
                .withPassword("postgres");
        postgres.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void register_shouldReturnCreated_whenDataIsValid() throws Exception {
        String login = "login-" + UUID.randomUUID();
        UserRequestDto userRequestDto = new UserRequestDto(NAME, SURNAME, BIRTH_DATE, randomEmail());
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(login, PASSWORD, ROLE, userRequestDto);

        when(userClient.createUserInUserService(any()))
                .thenReturn(new UserResponseDto(
                        USER_ID_SEQUENCE.getAndIncrement(),
                        NAME,
                        SURNAME,
                        BIRTH_DATE,
                        true,
                        userRequestDto.email(),
                        CURRENT_DATE,
                        CURRENT_DATE));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_shouldReturnConflict_whenUserServiceCallFails() throws Exception {
        String login = "login-" + UUID.randomUUID();
        UserRequestDto userRequestDto = new UserRequestDto(NAME, SURNAME, BIRTH_DATE, randomEmail());
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(login, PASSWORD, ROLE, userRequestDto);

        when(userClient.createUserInUserService(any()))
                .thenThrow(new RuntimeException("userservice unavailable"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() throws Exception {
        String login = "login-" + UUID.randomUUID();
        UserRequestDto userRequestDto = new UserRequestDto(NAME, SURNAME, BIRTH_DATE, randomEmail());
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(login, PASSWORD, ROLE, userRequestDto);

        when(userClient.createUserInUserService(any()))
                .thenReturn(new UserResponseDto(
                        USER_ID_SEQUENCE.getAndIncrement(),
                        NAME,
                        SURNAME,
                        BIRTH_DATE,
                        true,
                        userRequestDto.email(),
                        CURRENT_DATE,
                        CURRENT_DATE));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginRequestDto = new LoginRequestDto(login, PASSWORD);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponseDto actual = objectMapper.readValue(response, AuthResponseDto.class);

        assertNotNull(actual.accessToken());
        assertNotNull(actual.refreshToken());
    }

    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsWrong() throws Exception {
        String login = "login-" + UUID.randomUUID();
        UserRequestDto userRequestDto = new UserRequestDto(NAME, SURNAME, BIRTH_DATE, randomEmail());
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(login, PASSWORD, ROLE, userRequestDto);

        when(userClient.createUserInUserService(any()))
                .thenReturn( new UserResponseDto(
                        USER_ID_SEQUENCE.getAndIncrement(),
                        NAME,
                        SURNAME,
                        BIRTH_DATE,
                        true,
                        userRequestDto.email(),
                        CURRENT_DATE,
                        CURRENT_DATE));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginRequestDto = new LoginRequestDto(login, "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturnUnauthorized_whenLoginDoesNotExist() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("non-existent-" + UUID.randomUUID(), PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturnNewAccessToken_whenRefreshTokenIsValid() throws Exception {
        String login = "login-" + UUID.randomUUID();
        UserRequestDto userRequestDto = new UserRequestDto(NAME, SURNAME, BIRTH_DATE, randomEmail());
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(login, PASSWORD, ROLE, userRequestDto);

        when(userClient.createUserInUserService(any()))
                .thenReturn(new UserResponseDto(
                        USER_ID_SEQUENCE.getAndIncrement(),
                        NAME,
                        SURNAME,
                        BIRTH_DATE,
                        true,
                        userRequestDto.email(),
                        CURRENT_DATE,
                        CURRENT_DATE));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginRequestDto = new LoginRequestDto(login, PASSWORD);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponseDto loginResult = objectMapper.readValue(loginResponse, AuthResponseDto.class);

        RefreshRequestDto refreshRequestDto = new RefreshRequestDto(loginResult.refreshToken());

        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponseDto refreshResult = objectMapper.readValue(refreshResponse, AuthResponseDto.class);

        assertNotNull(refreshResult.accessToken());
        assertEquals(loginResult.refreshToken(), refreshResult.refreshToken());
    }

    @Test
    void refresh_shouldReturnUnauthorized_whenRefreshTokenIsInvalid() throws Exception {
        RefreshRequestDto refreshRequestDto = new RefreshRequestDto("invalid-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    private String randomEmail() {
        return UUID.randomUUID() + TEST_EMAIL_DOMAIN;
    }
}
