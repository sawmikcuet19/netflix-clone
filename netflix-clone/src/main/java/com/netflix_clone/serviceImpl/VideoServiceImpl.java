package com.netflix_clone.serviceImpl;

import com.netflix_clone.dto.request.VideoRequest;
import com.netflix_clone.dto.response.MessageResponse;
import com.netflix_clone.dto.response.PageResponse;
import com.netflix_clone.dto.response.VideoResponse;
import com.netflix_clone.dto.response.VideoStatsResponse;
import com.netflix_clone.entity.Video;
import com.netflix_clone.repositories.UserRepository;
import com.netflix_clone.repositories.VideoRepository;
import com.netflix_clone.service.VideoService;
import com.netflix_clone.util.PaginationUtils;
import com.netflix_clone.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final ServiceUtils serviceUtils;

    @Override
    @Transactional
    public MessageResponse createVideoByAdmin(VideoRequest videoRequest) {
        logger.info("Creating video by admin: title={}, srcUuid={}, posterUuid={}",
                videoRequest.getTitle(), videoRequest.getSrc(), videoRequest.getPoster());

        Video video = new Video();
        video.setTitle(videoRequest.getTitle());
        video.setDescription(videoRequest.getDescription());
        video.setYear(videoRequest.getYear());
        video.setRating(videoRequest.getRating());
        video.setDuration(videoRequest.getDuration());
        video.setSrcUuid(videoRequest.getSrc());
        video.setPosterUuid(videoRequest.getPoster());
        video.setPublished(videoRequest.isPublished());
        video.setCategories(videoRequest.getCategories() != null ? videoRequest.getCategories() : List.of());

        logger.info("Saving video entity: {}", video);
        try {
            Video saved = videoRepository.save(video);
            logger.info("Video saved successfully with id={}", saved.getId());
            return new MessageResponse("Video created successfully");
        } catch (Exception e) {
            logger.error("Failed to save video: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public PageResponse<VideoResponse> getAllAdminVideos(int page, int size, String search) {
        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");
        Page<Video> videoPage;
        if (search != null && !search.trim().isEmpty()) {
            videoPage = videoRepository.searchVideos(search, pageable);
        } else {
            videoPage = videoRepository.findAll(pageable);
        }
        return PaginationUtils.toPageResponse(videoPage, VideoResponse::fromEntity);
    }

    @Override
    public MessageResponse updateVideoByAdmin(Long id, VideoRequest videoRequest) {
        Video video = new Video();
        video.setId(id);
        video.setTitle(videoRequest.getTitle());
        video.setDescription(videoRequest.getDescription());
        video.setYear(videoRequest.getYear());
        video.setRating(videoRequest.getRating());
        video.setDuration(videoRequest.getDuration());
        video.setSrcUuid(videoRequest.getSrc());
        video.setPosterUuid(videoRequest.getPoster());
        video.setPublished(videoRequest.isPublished());
        video.setCategories(videoRequest.getCategories() != null ? videoRequest.getCategories() : List.of());

        videoRepository.save(video);
        return new MessageResponse("Video updated successfully");
    }

    @Override
    public MessageResponse deleteVideoByAdmin(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new IllegalArgumentException("Videos not found "+id);
        }
        videoRepository.deleteById(id);
        return new MessageResponse("Video deleted successfully");
    }

    @Override
    public MessageResponse toggleVideoPublishStatusByAdmin(Long id, boolean status) {
        Video video = serviceUtils.getVideoByIdOrThrow(id);
        video.setPublished(status);

        videoRepository.save(video);
        return new MessageResponse("Video publish status updated successfully");
    }

    @Override
    public VideoStatsResponse getAdminStats() {
        long totalVideos = videoRepository.count();
        long publishedVideos = videoRepository.countByPublishedTrue();
        long totalDuration = videoRepository.getTotalDuration();

        return new VideoStatsResponse(totalVideos, publishedVideos, totalDuration);
    }

    @Override
    public PageResponse<VideoResponse> getPublishedVideos(int page, int size, String search, String email) {
        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");
        Page<Video> videoPage;
        if (search != null && !search.trim().isEmpty()) {
            videoPage = videoRepository.searchPublishedVideos(search.trim(), pageable);
        } else {
            videoPage = videoRepository.findByPublishedVideos(pageable);
        }

        List<Video> videos = videoPage.getContent();
        Set<Long> watchListIds = Set.of();
        if (!videos.isEmpty()) {
            try {
                List<Long> videoIds = videos.stream().map(Video::getId).toList();
                watchListIds = userRepository.findWatchListVideoIds(email, videoIds);
            } catch (Exception e) {
                watchListIds = Set.of();
            }

        }
        Set<Long> finalWatchListIds = watchListIds;
        videos.forEach(video -> video.setIsInWatchlist(finalWatchListIds.contains(video.getId())));

        List<VideoResponse> videoResponses = videos.stream().map(VideoResponse::fromEntity).toList();
        return PaginationUtils.toPageResponse(videoPage, videoResponses);
    }

    @Override
    public List<VideoResponse> getFeaturedVideos() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Video> videos = videoRepository.findRandomPublishedVideos(pageable);
        //List<Video> videos = videoPage.getContent();
        return videos.stream().map(VideoResponse::fromEntity).toList();
    }

}
