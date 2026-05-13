package com.inventorymanagement.backend.service.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX  = "bl_token";

    /**
     * Blacklist a token. The TTL is set to the token's remaining validity,
     * so the entry is automatically deleted when the token expires.
     */
    public void blackListToken(String token, long remainingValidityMs) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(remainingValidityMs));
    }

    public boolean isTokenBlackListed(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
