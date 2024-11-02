package com.elsa.authcore.AuthCore.service;

import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private final PasswordEncoder passwordEncoder;

    // Inject PasswordEncoder directly
    public TokenService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generateToken() {
        String token = UUID.randomUUID().toString();
        String encodedToken = Base64.getEncoder().encodeToString(token.getBytes());
        logger.debug("Generated token: {}", encodedToken);
        return encodedToken;
    }

    public String hashToken(String token) {
        String hashedToken = passwordEncoder.encode(token);
        logger.debug("Hashed token: {}", hashedToken);
        return hashedToken;
    }

    public boolean verifyToken(String rawToken, String hashedToken) {
        boolean isMatch = passwordEncoder.matches(rawToken, hashedToken);
        logger.debug("Token verification result for {}: {}", rawToken, isMatch);
        return isMatch;
    }
}
