package com.backend.secureuserapi.security.jwt;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, Instant expiry){
        blacklistedTokens.put(token, expiry);
    }

    public boolean isBlacklisted(String token){
        cleanupExpiredTokens();
        return blacklistedTokens.containsKey(token);
    }

    private void cleanupExpiredTokens(){
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
