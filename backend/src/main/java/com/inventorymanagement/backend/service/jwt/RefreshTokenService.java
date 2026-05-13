package com.inventorymanagement.backend.service.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inventorymanagement.backend.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${application.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private static final String REFRESH_PREFIX = "refresh_token";
    private static final SecureRandom secureRandom = new SecureRandom();

    public String createRefreshToken(String username, String role) {
        String token = generateRandomToken();
        String key = REFRESH_PREFIX + token;

        TokenInfo tokenInfo = new TokenInfo(token, role);
        try{
            String json = objectMapper.writeValueAsString(tokenInfo);
            redisTemplate.opsForValue().set(key, json, Duration.ofMillis(refreshExpirationMs));
            return token;
        }catch (Exception e){
            throw new RuntimeException("Failed to store Refresh Token: "+ e.getMessage());
        }
    }

    public TokenInfo rotateRefreshToken(String token) {
        String key = REFRESH_PREFIX + token;
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            throw new InvalidRefreshTokenException("Refresh token not found or expired");
        }

        // Delete old token (one‑time use)
        redisTemplate.delete(key);

        return objectMapper.readValue(json, TokenInfo.class);
    }

    public void revokeRefreshToken(String token) {
        redisTemplate.delete(REFRESH_PREFIX + token);
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record TokenInfo(String username, String role) {

    }
}
