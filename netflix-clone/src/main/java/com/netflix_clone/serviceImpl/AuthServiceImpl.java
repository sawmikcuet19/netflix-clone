package com.netflix_clone.serviceImpl;

import com.netflix_clone.dto.request.LoginRequest;
import com.netflix_clone.dto.request.UserRequest;
import com.netflix_clone.dto.response.EmailValidationResponse;
import com.netflix_clone.dto.response.LoginResponse;
import com.netflix_clone.dto.response.MessageResponse;
import com.netflix_clone.entity.User;
import com.netflix_clone.enums.Role;
import com.netflix_clone.exception.*;
import com.netflix_clone.repositories.UserRepository;
import com.netflix_clone.security.JwtUtil;
import com.netflix_clone.service.AuthService;
import com.netflix_clone.service.EmailService;
import com.netflix_clone.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ServiceUtils serviceUtils;


    @Override
    public MessageResponse signup(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw  new EmailAlreadyExistsException("User already exists");
        }

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFullName(userRequest.getFullName());
        user.setRole(Role.USER);
        user.setActive(true);
        user.setEmailVerified(false);

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400));

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return new MessageResponse("User registered successfully! Please check your email for verification.");
    }

    @Override
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new BadCredentialException("Invalid email or password"));

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email not verified, Please verify before logging in. Check your inbox for the verification link");
        }
        final String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    @Override
    public EmailValidationResponse validateEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        return new EmailValidationResponse(exists, !exists);
    }

    @Override
    public MessageResponse verifyEmail(String token) {
        logger.info("Verifying email with token: {}", token);

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> {
                    logger.error("Token not found in database: {}", token);
                    return new InvalidTokenException("Token not found in database");
                });

        logger.info("User found: email={}, emailVerified={}, tokenExpiry={}",
                user.getEmail(), user.isEmailVerified(), user.getVerificationTokenExpiry());

        if (user.isEmailVerified()) {
            logger.warn("Email already verified for user: {}", user.getEmail());
            return new MessageResponse("Email already verified. Please login.");
        }

        if (user.getVerificationTokenExpiry() == null) {
            logger.error("Token expiry is null for user: {}", user.getEmail());
            throw new InvalidTokenException("Invalid token configuration");
        }

        if (user.getVerificationTokenExpiry().isBefore(Instant.now())) {
            logger.error("Token expired. Expiry: {}, Now: {}",
                    user.getVerificationTokenExpiry(), Instant.now());
            throw new InvalidTokenException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        logger.info("Email verified successfully for: {}", user.getEmail());

        return new MessageResponse("Email verified successfully, you can now login");
    }

    @Override
    public MessageResponse resendVerification(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400));
        userRepository.save(user);
        emailService.sendVerificationEmail(email, verificationToken);

        return new MessageResponse("Verification email sent successfully, please check your inbox");
    }

    @Override
    public MessageResponse forgotPassword(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(Instant.now().plusSeconds(3600));
        userRepository.save(user);
        emailService.sendPasswordResetEmail(email, resetToken);

        return new MessageResponse("Password reset email sent successfully, please check your inbox");
    }

    @Override
    public MessageResponse resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid password or Expired reset token"));

        if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(Instant.now())){
            throw new InvalidTokenException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        return new MessageResponse("Password reset successfully, you can now login with your new password");
    }

    @Override
    public MessageResponse changePassword(String email, String currentPassword, String newPassword) {
        User user = serviceUtils.getUserByEmailOrThrow(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new MessageResponse("Password changed successfully");
    }

    @Override
    public LoginResponse currentUser(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);

        return new LoginResponse(null, user.getEmail(), user.getFullName(), user.getRole().name());
    }


}
