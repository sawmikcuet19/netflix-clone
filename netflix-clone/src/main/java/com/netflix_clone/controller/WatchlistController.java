package com.netflix_clone.controller;

import com.netflix_clone.dto.response.MessageResponse;
import com.netflix_clone.dto.response.PageResponse;
import com.netflix_clone.dto.response.VideoResponse;
import com.netflix_clone.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {
    private final WatchlistService watchlistService;

    @PostMapping("/{videoId}")
    public ResponseEntity<MessageResponse> addToWatchlist(
            @PathVariable Long videoId,
            Authentication authentication) {
        String email = authentication.getName();

        return ResponseEntity.ok(watchlistService.addToWatchlist(email, videoId));
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<MessageResponse> removeFromWatchlist(
            @PathVariable Long videoId,
            Authentication authentication) {
        String email = authentication.getName();

        return ResponseEntity.ok(watchlistService.removeFromWatchlist(email, videoId));
    }

    @DeleteMapping
    public ResponseEntity<PageResponse<VideoResponse>> getWatchlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String email = authentication.getName();
        PageResponse<VideoResponse> response = watchlistService.getWatchlistPaginated(page, size, search, email);
        return ResponseEntity.ok(response);
    }
}
