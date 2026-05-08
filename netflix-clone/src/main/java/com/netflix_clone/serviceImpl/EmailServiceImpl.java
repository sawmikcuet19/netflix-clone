package com.netflix_clone.serviceImpl;

import com.netflix_clone.exception.EmailNotVerifiedException;
import com.netflix_clone.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender javaMailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Netflix Clone - Verify Your Email");

            String verificationLink = frontendUrl + "/verify-email?token=" + token;

            String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #141414;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td align="center" style="padding: 40px 0;">
                                <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #1f1f1f; border-radius: 8px;">
                                    <tr>
                                        <td style="padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #e50914; margin: 0 0 20px 0; font-size: 28px;">NETFLIX CLONE</h1>
                                            <h2 style="color: #ffffff; margin: 0 0 30px 0; font-size: 22px;">Verify Your Email</h2>
                                            <p style="color: #b3b3b3; font-size: 16px; line-height: 1.5; margin: 0 0 30px 0;">
                                                Welcome! Thank you for registering. Please verify your email address by clicking the button below.
                                            </p>
                                            <a href="%s" style="display: inline-block; background-color: #e50914; color: #ffffff; text-decoration: none; padding: 15px 40px; border-radius: 4px; font-size: 16px; font-weight: bold;">Verify Email</a>
                                            <p style="color: #737373; font-size: 14px; margin: 30px 0 20px 0;">
                                                This link will expire in 24 hours
                                            </p>
                                            <p style="color: #737373; font-size: 12px; line-height: 1.5; margin: 0;">
                                                If you did not register for Netflix Clone, please ignore this email.<br>
                                                If the button doesn't work, copy and paste this link into your browser:<br>
                                                <a href="%s" style="color: #e50914; text-decoration: underline;">%s</a>
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                                <p style="color: #737373; font-size: 12px; margin-top: 20px;">
                                    Best regards,<br>The Netflix Clone Team
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(verificationLink, verificationLink, verificationLink);

            helper.setText(htmlBody, true);
            javaMailSender.send(message);
            logger.info("Verification email sent to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage(), e);
            throw new EmailNotVerifiedException("Failed to send verification email");
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Netflix Clone - Password Reset");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #141414;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td align="center" style="padding: 40px 0;">
                                <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #1f1f1f; border-radius: 8px;">
                                    <tr>
                                        <td style="padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #e50914; margin: 0 0 20px 0; font-size: 28px;">NETFLIX CLONE</h1>
                                            <h2 style="color: #ffffff; margin: 0 0 30px 0; font-size: 22px;">Reset Your Password</h2>
                                            <p style="color: #b3b3b3; font-size: 16px; line-height: 1.5; margin: 0 0 30px 0;">
                                                You have requested to reset your password. Click the button below to set a new password.
                                            </p>
                                            <a href="%s" style="display: inline-block; background-color: #e50914; color: #ffffff; text-decoration: none; padding: 15px 40px; border-radius: 4px; font-size: 16px; font-weight: bold;">Reset Password</a>
                                            <p style="color: #737373; font-size: 14px; margin: 30px 0 20px 0;">
                                                This link will expire in 1 hour
                                            </p>
                                            <p style="color: #737373; font-size: 12px; line-height: 1.5; margin: 0;">
                                                If you did not request a password reset, please ignore this email.<br>
                                                If the button doesn't work, copy and paste this link into your browser:<br>
                                                <a href="%s" style="color: #e50914; text-decoration: underline;">%s</a>
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                                <p style="color: #737373; font-size: 12px; margin-top: 20px;">
                                    Best regards,<br>The Netflix Clone Team
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(resetLink, resetLink, resetLink);

            helper.setText(htmlBody, true);
            javaMailSender.send(message);
            logger.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new EmailNotVerifiedException("Failed to send password reset email");
        }
    }
}
