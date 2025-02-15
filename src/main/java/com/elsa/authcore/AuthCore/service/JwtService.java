package com.elsa.authcore.AuthCore.service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

	@Value("${security.jwt.secret-key}")
    private String SECRET;

    // Generate token with given username or email
    public String generateToken(String usernameOrEmail) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, usernameOrEmail);
    }

    // Create a JWT token with specified claims and subject (username or email)
    private String createToken(Map<String, Object> claims, String usernameOrEmail) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usernameOrEmail)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // Token valid for 30 minutes
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Get the signing key for JWT token
    private Key getSignKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract the username or email from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract the expiration date from the token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract a claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if the token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate the token against user details and expiration
    public Boolean validateToken(String token, CustomUserDetails userDetails) {
        final String usernameOrEmail = extractUsername(token);
        return (usernameOrEmail.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Additional method to check if the token is valid for given user details
    public Boolean validateToken(String token, String usernameOrEmail) {
        return (usernameOrEmail.equals(extractUsername(token)) && !isTokenExpired(token));
    }

    public String refreshToken(String oldToken) {
        String usernameOrEmail = extractUsername(oldToken); // Extract username from token
        if (validateToken(oldToken, usernameOrEmail)) { // Validate the token using usernameOrEmail
            return generateToken(usernameOrEmail); // Generate a new token with updated expiration
        } else {
            throw new IllegalArgumentException("Invalid Token");
        }
    }



}
