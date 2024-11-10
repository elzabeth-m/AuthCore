package com.elsa.authcore.AuthCore.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.elsa.authcore.AuthCore.model.User;

public class CustomUserDetails implements UserDetails {

    private String usernameOrEmail; // Store either username or email
    private String password;
    private boolean enabled;
    private int failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLogin;

    public CustomUserDetails(User user) {
        this.usernameOrEmail = user.getUsername(); // Store username
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.failedAttempts = user.getFailedAttempts();
        this.lockedUntil = user.getLockedUntil();
        this.lastLogin = user.getLastLogin();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // No roles defined, return an empty list
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return usernameOrEmail; // Return the username or email for authentication
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Implement your logic if you need this
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || LocalDateTime.now().isAfter(lockedUntil);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Implement your logic if you need this
    }

    @Override
    public boolean isEnabled() {
        return enabled; // Return the user's enabled status
    }

    // Additional getters for user details
    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
}