package com.elsa.authcore.AuthCore.dto;

import jakarta.validation.constraints.NotBlank;

public class VerificationDto {
	@NotBlank(message = "Token is required")
    private String verificationToken;

	public String getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}
}
