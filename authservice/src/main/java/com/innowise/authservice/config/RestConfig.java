package com.innowise.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import org.springframework.boot.restclient.RestTemplateBuilder;

import java.time.Duration;

@Configuration
public class RestConfig {

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .baseUri(userServiceUrl)
                .build();
    }
}
