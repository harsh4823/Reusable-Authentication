package com.auth.auth_app.service.impl;

import com.auth.auth_app.entity.AuthUser;
import com.auth.auth_app.entity.RefreshToken;
import com.auth.auth_app.repository.AuthUserRepository;
import com.auth.auth_app.repository.RefreshTokenRepository;
import com.auth.auth_app.service.IRefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImp implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthUserRepository authUserRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        AuthUser authUser = authUserRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));

        long REFRESH_TOKEN_EXPIRATION_MS = 604800000L;
        RefreshToken refreshToken = RefreshToken.builder()
                .authUser(authUser)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findRefreshTokenByToken(token);
    }
}
