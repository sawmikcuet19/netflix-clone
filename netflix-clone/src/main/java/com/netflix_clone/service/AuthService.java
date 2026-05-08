package com.netflix_clone.service;

import com.netflix_clone.dto.request.EmailRequest;
import com.netflix_clone.dto.request.LoginRequest;
import com.netflix_clone.dto.request.UserRequest;
import com.netflix_clone.dto.response.EmailValidationResponse;
import com.netflix_clone.dto.response.LoginResponse;
import com.netflix_clone.dto.response.MessageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface AuthService {
    MessageResponse signup(@Valid UserRequest userRequest);

    LoginResponse login( String email, String password);

    EmailValidationResponse validateEmail(String email);

    MessageResponse verifyEmail(String token);

    MessageResponse resendVerification(String email);

    MessageResponse forgotPassword(String email);

    MessageResponse resetPassword(String token, String newPassword);

    MessageResponse changePassword(String email, String currentPassword, String newPassword);

    LoginResponse currentUser(String email);
}
