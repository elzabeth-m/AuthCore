package com.elsa.authcore.AuthCore.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.management.InvalidAttributeValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.elsa.authcore.AuthCore.Exception.InvalidTokenException;
import com.elsa.authcore.AuthCore.Exception.TokenExpiredException;
import com.elsa.authcore.AuthCore.Exception.UserAlreadyExistsException;
import com.elsa.authcore.AuthCore.Exception.UserNotFoundException;
import com.elsa.authcore.AuthCore.dto.ForgetPasswordDto;
import com.elsa.authcore.AuthCore.dto.RegisterDto;
import com.elsa.authcore.AuthCore.dto.ResetPasswordDto;
import com.elsa.authcore.AuthCore.dto.VerificationDto;
import com.elsa.authcore.AuthCore.model.User;
import com.elsa.authcore.AuthCore.repository.UserRepository;


import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class UserService implements UserDetailsService{

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  EmailService emailService;
    @Autowired
    private  TokenService tokenService;
    @Autowired
    private  PasswordEncoder passwordEncoder;

    
  

    @Transactional
    public void requestPasswordReset(@Valid ForgetPasswordDto input) {
        if (input == null || input.getUsernameOrEmail().isBlank()) {
            throw new IllegalArgumentException("Username or email must not be empty.");
        }

        try {
            Optional<User> userOptional = userRepository.findByUsernameOrEmail(input.getUsernameOrEmail());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                logger.info("Password reset requested for user: {}", user.getUsername());

                // Generate and store reset token
                String resetToken = tokenService.generateToken();
                String hashedToken = tokenService.hashToken(resetToken);
                user.setResetToken(hashedToken);
                user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

                // Log the user state before saving
                logger.debug("User before saving: {}", user);

                userRepository.save(user); // This line might be causing the issue

                // Create and send the reset link
                String resetLink = String.format("http://localhost:8080/api/reset?token=%s", resetToken);
                emailService.sendVerificationEmail(user.getEmail(), "Password Reset Request", "To reset your password, click the link below:\n" + resetLink);
                logger.info("Password reset email sent to: {}", user.getEmail());
            } else {
                logger.warn("Password reset attempt failed: User not found for {}", input.getUsernameOrEmail());
                throw new UserNotFoundException("User not found with that username or email.");
            }
        } catch (Exception e) {
            logger.error("Unexpected error while processing password reset request", e); // This will show the full stack trace
            throw new RuntimeException("Could not commit JPA transaction: " +  e);
        }

    }
    
    
    @Transactional
    public void resetPassword(String token, ResetPasswordDto resetPasswordDto) throws InvalidTokenException, TokenExpiredException, InvalidAttributeValueException {
        User user = getUserByResetToken(token);
        
        validateTokenExpiry(user);
        validateNewPassword(resetPasswordDto.getNewPassword(), user.getPassword());

        updateUserPassword(user, resetPasswordDto.getNewPassword());

        logger.info("Password has been successfully reset for user: {}", user.getUsername());
    }

    private User getUserByResetToken(String plainToken) throws InvalidTokenException {
        // Retrieve users with valid reset tokens (not expired)
        List<User> usersWithValidTokens = userRepository.findUsersWithValidResetTokens();

        // Filter the list by matching the hashed token
        return usersWithValidTokens.stream()
            .filter(user -> passwordEncoder.matches(plainToken, user.getResetToken()))
            .findFirst()
            .orElseThrow(() -> {
                logger.warn("Invalid password reset attempt: Token is invalid.");
                return new InvalidTokenException("Invalid token.");
            });
    }

    // Other helper methods remain the same

    private void validateTokenExpiry(User user) throws TokenExpiredException {
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            logger.warn("Password reset attempt failed: Token has expired for user {}.", user.getUsername());
            throw new TokenExpiredException("Token has expired.");
        }
    }

    private void validateNewPassword(String newPassword, String currentPassword) throws InvalidAttributeValueException {
        if (passwordEncoder.matches(newPassword, currentPassword)) {
            logger.warn("Password reset attempt failed: New password must be different from the old password.");
            throw new InvalidAttributeValueException("New password must be different from the old password.");
        }
    }

    private void updateUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    
    
    //login 
    
    
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
        		.map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
        
        // Return an instance of CustomUserDetails
    }

//register 
    
	public User registerUser(@Valid RegisterDto input) {
		// Check if the user already exists
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists.");
        }

		User user= new User();
		user.setEmail(input.getEmail());
		user.setPassword(input.getPassword());
		user.setUsername(input.getUsername());
		user.setPassword(passwordEncoder.encode(input.getPassword()));
		user.setVerificationToken(tokenService.generateToken());
		user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(15) );
		
		sendVerificationEmail(user);
		
		return userRepository.save(user);
	}


	private void sendVerificationEmail(User user) {
		String subject = "Account Verification";
        String verificationLink = "http://localhost:8080/api/verify?token=" + user.getVerificationToken();
        
        String content = "<html>"
                + "<body>"
                + "<p>Click the link below to verify your account:</p>"
                + "<a href=\"" + verificationLink + "\">Verify your account</a>"
                + "</body>"
                + "</html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email.", e);
        }
    }


	public void verifyUser(VerificationDto input) {
	Optional<User> optionalUser = userRepository.findByVerificationToken(input.getVerificationToken());
		User user=optionalUser.get();
		if(user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
			throw new TokenExpiredException("Verification token has expired.");
		}
		user.setEnabled(true);
		user.setVerificationToken(null);
		user.setTokenExpiryDate(null);
		userRepository.save(user);
	}
	
	
		
	}


