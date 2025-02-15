package com.elsa.authcore.AuthCore.dto;

import jakarta.validation.constraints.NotBlank;

public class ForgetPasswordDto {
	@NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

	public String getUsernameOrEmail() {
		return usernameOrEmail;
	}

	public void setUsernameOrEmail(String usernameOrEmail) {
		this.usernameOrEmail = usernameOrEmail;
	}
	// Method to check if the input is a valid email format
    public boolean isValidEmail() {
        return usernameOrEmail != null && usernameOrEmail.matches("^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }
}
