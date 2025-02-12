package com.elsa.authcore.AuthCore.controller;

import javax.management.InvalidAttributeValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elsa.authcore.AuthCore.Exception.InvalidTokenException;
import com.elsa.authcore.AuthCore.Exception.TokenExpiredException;
import com.elsa.authcore.AuthCore.Exception.UserNotFoundException;
import com.elsa.authcore.AuthCore.dto.AuthResponse;
import com.elsa.authcore.AuthCore.dto.ForgetPasswordDto;
import com.elsa.authcore.AuthCore.dto.LoginDto;
import com.elsa.authcore.AuthCore.dto.RegisterDto;
import com.elsa.authcore.AuthCore.dto.ResetPasswordDto;
import com.elsa.authcore.AuthCore.dto.ResponseWrapper;
import com.elsa.authcore.AuthCore.dto.VerificationDto;
import com.elsa.authcore.AuthCore.service.JwtService;
import com.elsa.authcore.AuthCore.service.UserService;


import jakarta.validation.Valid;
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/forgotpassword")
    public ResponseEntity<ResponseWrapper<String>> forgotPassword(@Valid @RequestBody ForgetPasswordDto forgotPasswordDto) {
        try {
            userService.requestPasswordReset(forgotPasswordDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseWrapper<>(true, "Email sent successfully. Please check your inbox.", null));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseWrapper<>(false, null, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>(false, null, e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while processing password reset request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(false, null, "An unexpected error occurred."));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ResponseWrapper<String>> resetPassword(
            @RequestParam("token") String token,
            @Validated @RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            userService.resetPassword(token, resetPasswordDto);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Password has been successfully reset.", null));
        } catch (InvalidTokenException | TokenExpiredException | InvalidAttributeValueException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>(false, null, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<AuthResponse>> authenticateAndGetToken(@Validated @RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword())
        );

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(loginDto.getUsernameOrEmail());
            return ResponseEntity.ok(new ResponseWrapper<>(true, new AuthResponse(token), "Login successful"));
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<String>> registerUser(@Valid @RequestBody RegisterDto registerDto) {
        try {
            userService.registerUser(registerDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseWrapper<>(true, "User registered successfully. Please check your email to verify your account.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>(false, null, e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while processing user registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(false, null, "An unexpected error occurred."));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<ResponseWrapper<String>> verifyUser(@RequestParam("token") String token) {
        try {
            VerificationDto verificationDto = new VerificationDto();
            verificationDto.setVerificationToken(token);
            userService.verifyUser(verificationDto);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Account verified successfully.", null));
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>(false, null, e.getMessage()));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>(false, null, "User not found with the provided verification token."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(false, null, "An unexpected error occurred."));
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseWrapper<AuthResponse>> refreshToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
        }
        String newToken = jwtService.refreshToken(token);
        return ResponseEntity.ok(new ResponseWrapper<>(true, new AuthResponse(newToken), "Token refreshed successfully"));
    }
}
