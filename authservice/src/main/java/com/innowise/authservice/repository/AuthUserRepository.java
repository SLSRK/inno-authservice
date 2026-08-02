package com.innowise.authservice.repository;

import com.innowise.authservice.model.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByLogin(String login);

    Optional<AuthUser> findByUserId(Long userId);
}
