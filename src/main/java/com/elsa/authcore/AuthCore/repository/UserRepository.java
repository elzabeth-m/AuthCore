package com.elsa.authcore.AuthCore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.elsa.authcore.AuthCore.dto.ForgetPasswordDto;
import com.elsa.authcore.AuthCore.model.User;


import jakarta.validation.Valid;

@Repository
public interface UserRepository extends JpaRepository<User,Integer>{
	
	Optional<User> findByEmail(String email);
	
	Optional<User> findByVerificationToken(String token);
	
	@Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
	Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

	
	   // Only fetch users with a valid (non-expired) reset token
    @Query("SELECT u FROM User u WHERE u.resetToken IS NOT NULL AND u.resetTokenExpiry > CURRENT_TIMESTAMP")
    List<User> findUsersWithValidResetTokens();
}
