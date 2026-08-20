package com.innowise.authservice.service.impl;

import com.innowise.authservice.model.entity.Role;
import com.innowise.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${access.expiration.mins}") long accessExpiration,
            @Value("${refresh.expiration.mins}") long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String createAccessToken(Long userId, Role role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration * 60000))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration * 60000))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public Claims validateToken(String token, String requiredType) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!requiredType.equals(claims.get("type", String.class))) {
            throw new JwtException("Invalid token type");
        }

        return claims;
    }

    public Long getUserId(String token, String tokenType) {
        return Long.valueOf(validateToken(token, tokenType).getSubject());
    }
}
