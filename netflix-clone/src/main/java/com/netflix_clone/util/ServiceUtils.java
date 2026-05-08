package com.netflix_clone.util;

import com.netflix_clone.entity.User;
import com.netflix_clone.entity.Video;
import com.netflix_clone.exception.ResourceNotFoundException;
import com.netflix_clone.repositories.UserRepository;
import com.netflix_clone.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceUtils {
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email" + email));
    }

    public User getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id" + id));
    }

    public Video getVideoByIdOrThrow(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id" + id));
    }
}
