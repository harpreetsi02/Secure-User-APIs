package com.backend.secureuserapi.service;

import com.backend.secureuserapi.entity.RefreshToken;
import com.backend.secureuserapi.entity.User;
import com.backend.secureuserapi.exception.InvalidCredentialException;
import com.backend.secureuserapi.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(User user){

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                user,
                Instant.now().plusMillis(refreshExpirationMs)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token){

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new InvalidCredentialException(
                                "Invalid refresh token: " + token
                        )
                );

        if (refreshToken.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialException(
                    "Refresh token expired, please login again."
            );
        }

        return refreshToken;
    }

    public void deleteByUserId(Long userId){
        refreshTokenRepository.deleteByUserId(userId);
    }
}
