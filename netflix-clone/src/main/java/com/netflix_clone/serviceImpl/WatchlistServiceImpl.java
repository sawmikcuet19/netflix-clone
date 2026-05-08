package com.netflix_clone.serviceImpl;

import com.netflix_clone.dto.response.MessageResponse;
import com.netflix_clone.dto.response.PageResponse;
import com.netflix_clone.dto.response.VideoResponse;
import com.netflix_clone.entity.User;
import com.netflix_clone.entity.Video;
import com.netflix_clone.repositories.UserRepository;
import com.netflix_clone.repositories.VideoRepository;
import com.netflix_clone.service.WatchlistService;
import com.netflix_clone.util.PaginationUtils;
import com.netflix_clone.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final ServiceUtils serviceUtils;

    @Override
    public MessageResponse addToWatchlist(String email, Long videoId) {
        User user = serviceUtils.getUserByEmailOrThrow(email);

        Video video = serviceUtils.getVideoByIdOrThrow(videoId);

        user.addToWatchlist(video);
        userRepository.save(user);

        return new MessageResponse("Video added to watchlist successfully");
    }

    @Override
    public MessageResponse removeFromWatchlist(String email, Long videoId) {
        User user = serviceUtils.getUserByEmailOrThrow(email);

        Video video = serviceUtils.getVideoByIdOrThrow(videoId);

        user.removeFromWatchlist(video);
        userRepository.save(user);

        return new MessageResponse("Video removed from watchlist successfully");
    }

    @Override
    public PageResponse<VideoResponse> getWatchlistPaginated(int page, int size, String search, String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);

        Pageable pageable = PaginationUtils.createPageRequest(page, size);
        Page<Video> videoPage;

        if (search != null && !search.trim().isEmpty()) {
            videoPage = userRepository.searchWatchlistByUserId(user.getId(), search.trim(), pageable);
        } else {
            videoPage = userRepository.findWatchlistByUserId(user.getId(), pageable);
        }
        return PaginationUtils.toPageResponse(videoPage, VideoResponse::fromEntity);
    }
}
