package com.elsa.authcore.AuthCore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	

	    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	    @Autowired
	    private JavaMailSender emailSender;

	    public void sendVerificationEmail(String to, String subject, String content) {
	        try {
	            MimeMessage message = emailSender.createMimeMessage();
	            MimeMessageHelper helper = new MimeMessageHelper(message, true);

	            helper.setTo(to);
	            helper.setSubject(subject);
	            helper.setText(content, true);

	            emailSender.send(message);
	            logger.info("Verification email sent to {}", to);
	        } catch (MailException | MessagingException e) {
	            logger.error("Failed to send verification email to {}: {}", to, e.getMessage());
	            throw new RuntimeException("Could not send verification email. Please try again later.");
	        }
	    }
}
