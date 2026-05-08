package com.netflix_clone.service;

import com.netflix_clone.dto.request.UserRequest;
import com.netflix_clone.dto.response.MessageResponse;
import com.netflix_clone.dto.response.PageResponse;
import com.netflix_clone.dto.response.UserResponse;

public interface UserService {
    MessageResponse createUser(UserRequest userRequest);

    MessageResponse updateUser(Long id, UserRequest userRequest);

    PageResponse<UserResponse> getAllUsers(int page, int size, String search);

    MessageResponse deleteUser(Long id, String currentEmail);

    MessageResponse toggleStatus(Long id, String currentUserEmail);

    MessageResponse changeRole(Long id, UserRequest userRequest);
}
