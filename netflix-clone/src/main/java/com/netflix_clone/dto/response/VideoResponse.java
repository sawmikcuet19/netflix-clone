package com.netflix_clone.dto.response;

import com.netflix_clone.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {
    private Long id;
    private String title;
    private String description;
    private Integer year;
    private String rating;

    private Integer duration;
    private String src;
    private String poster;
    private boolean published;

    private List<String> categories;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isWatchlist;

    public VideoResponse (
       Long id,
       String title,
       String description,
       Integer year,
       String rating,
       Integer duration,
       String src,
       String poster,
       boolean published,
       List<String> categories,
       Instant createdAt,
       Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.year = year;
        this.rating = rating;
        this.duration = duration;
        this.src = src;
        this.poster = poster;
        this.published = published;
        this.categories = categories;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static VideoResponse fromEntity(Video video) {
        VideoResponse response = new VideoResponse(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getYear(),
                video.getRating(),
                video.getDuration(),
                video.getSrc(),
                video.getPoster(),
                video.isPublished(),
                video.getCategories(),
                video.getCreatedAt(),
                video.getUpdatedAt()
        );
        if (video.getIsInWatchlist() != null) {
            response.setIsWatchlist(video.getIsInWatchlist());
        }
        return response;
    }
}
