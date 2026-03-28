package com.eduScale.service;

import com.eduScale.config.JwtProperties;
import com.eduScale.controller.AuthController;
import com.eduScale.domain.RefreshToken;
import com.eduScale.domain.User;
import com.eduScale.repository.RefreshTokenRepository;
import com.eduScale.repository.UserRepository;
import com.eduScale.security.JwtService;
import com.eduScale.security.TokenHasher;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHasher tokenHasher;
    private final JwtProperties jwtProperties;

    public AuthController.AuthResponse login(String email, String rawPassword) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Parent account not found for this email."));
        if (user.getRole() != User.Role.PARENT) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "This account is not a parent account.");
        }
        if (!passwordMatches(user, rawPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Incorrect email or password.");
        }
        return issueTokens(user);
    }

    public AuthController.AuthResponse refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required.");
        }
        String hash = tokenHasher.hash(rawRefreshToken.trim());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid refresh token."));
        if (stored.getRevokedAt() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revoked.");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired.");
        }
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found."));
        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);
        return issueTokens(user);
    }

    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String hash = tokenHasher.hash(rawRefreshToken.trim());
        Optional<RefreshToken> opt = refreshTokenRepository.findByTokenHash(hash);
        opt.ifPresent(t -> {
            t.setRevokedAt(Instant.now());
            refreshTokenRepository.save(t);
        });
    }

    private AuthController.AuthResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user);
        String rawRefresh = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        String refreshHash = tokenHasher.hash(rawRefresh);
        Instant exp = Instant.now().plus(jwtProperties.getRefreshExpirationDays(), ChronoUnit.DAYS);
        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(refreshHash)
                .userId(user.getId())
                .expiresAt(exp)
                .build());
        return new AuthController.AuthResponse(
                access,
                rawRefresh,
                "Bearer",
                jwtService.getAccessExpirationMs() / 1000,
                AuthController.UserSummary.from(user));
    }

    private boolean passwordMatches(User user, String rawPassword) {
        String stored = user.getPassword();
        if (stored == null || stored.isBlank()) {
            return false;
        }
        if (passwordEncoder.matches(rawPassword, stored)) {
            return true;
        }
        if (stored.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
