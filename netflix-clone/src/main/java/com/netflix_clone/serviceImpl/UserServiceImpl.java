package com.netflix_clone.serviceImpl;

import com.netflix_clone.dto.request.UserRequest;
import com.netflix_clone.dto.response.MessageResponse;
import com.netflix_clone.dto.response.PageResponse;
import com.netflix_clone.dto.response.UserResponse;
import com.netflix_clone.entity.User;
import com.netflix_clone.enums.Role;
import com.netflix_clone.exception.EmailAlreadyExistsException;
import com.netflix_clone.exception.InvalidRoleException;
import com.netflix_clone.repositories.UserRepository;
import com.netflix_clone.service.EmailService;
import com.netflix_clone.service.UserService;

import com.netflix_clone.util.PaginationUtils;
import com.netflix_clone.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceUtils serviceUtils;
    private final EmailService emailService;
    
    @Override
    public MessageResponse createUser(UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        
        validateRole(userRequest.getRole());

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFullName(userRequest.getFullName());
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        user.setActive(true);
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400));
        userRepository.save(user);
        emailService.sendVerificationEmail(userRequest.getEmail(), verificationToken);
        
        return new MessageResponse("User created successfully");
    }

    @Override
    public MessageResponse updateUser(Long id, UserRequest userRequest) {
        User user = serviceUtils.getUserByIdOrThrow(id);
        
        ensureNotLastActiveAdmin(user);
        validateRole(userRequest.getRole());

        user.setFullName(userRequest.getFullName());
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        userRepository.save(user);
        
        return new MessageResponse("User updated successfully");
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size, String search) {
        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");

        Page<User> userPage;

        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.searchUsers(search.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        return PaginationUtils.toPageResponse(userPage, UserResponse::fromEntity);
    }

    @Override
    public MessageResponse deleteUser(Long id, String currentEmail) {
        User user = serviceUtils.getUserByIdOrThrow(id);

        if (user.getEmail().equals(currentEmail)) {
            throw new RuntimeException("Cannot delete your own account");
        }

        ensureNotLastAdmin(user, "delete");
        userRepository.deleteById(id);

        return new MessageResponse("User deleted successfully");
    }

    @Override
    public MessageResponse toggleStatus(Long id, String currentUserEmail) {
        User user = serviceUtils.getUserByIdOrThrow(id);

        if (user.getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Cannot deactivate your own account");
        }
        ensureNotLastActiveAdmin(user);
        user.setActive(!user.isActive());

        userRepository.save(user);

        return new MessageResponse("User status toggled successfully");
    }

    @Override
    public MessageResponse changeRole(Long id, UserRequest userRequest) {
        User user = serviceUtils.getUserByIdOrThrow(id);
        validateRole(userRequest.getRole());

        Role newRole = Role.valueOf(userRequest.getRole().toUpperCase());
        if (user.getRole() == Role.ADMIN && newRole == Role.USER) {
            ensureNotLastAdmin(user, "change role of ");
        }
        user.setRole(newRole);
        userRepository.save(user);

        return new MessageResponse("Role has changed Successfully ");
    }

    private void ensureNotLastAdmin(User user, String operation) {
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot " + operation + " last admin");
            }
        }
    }

    private void ensureNotLastActiveAdmin(User user) {
        if (user.isActive() && user.getRole() == Role.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndActive(Role.ADMIN, true);
            if (activeAdminCount <= 1) {
                throw new RuntimeException("Cannot deactivate last active admin");
            }

        }
    }

    private void validateRole(String role) {
        boolean isValid = Arrays.stream(Role.values())
                .anyMatch(r -> r.name().equalsIgnoreCase(role));
        if (!isValid) {
            throw new InvalidRoleException("Invalid Role " + role);
        }
    }

}
