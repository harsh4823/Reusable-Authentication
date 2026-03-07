package com.auth.auth_app.service;

import com.auth.auth_app.entity.RefreshToken;

import java.util.Optional;

public interface IRefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyRefreshToken(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
}
