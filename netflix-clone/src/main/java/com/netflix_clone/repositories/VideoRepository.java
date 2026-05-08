package com.netflix_clone.repositories;

import com.netflix_clone.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, Long> {
    @Query("SELECT v FROM Video v " +
            "WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(v.description) LIKE LOWER(CONCAT('%', :search, '%')) ")
    Page<Video> searchVideos(@Param("search") String search, Pageable pageable);

    long countByPublishedTrue();

    @Query("SELECT SUM(v.duration) FROM Video v")
    Long getTotalDuration();

    @Query("SELECT v FROM Video v " +
            "WHERE v.published = true AND (" +
            "LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(v.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY v.createdAt DESC")
    Page<Video> searchPublishedVideos(@Param("search") String search, Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.published = true ORDER BY v.createdAt DESC")
    Page<Video> findByPublishedVideos(Pageable pageable);

    @Query("SELECT v FROM Video v WHERE v.published = true ORDER BY FUNCTION('RAND')")
    Page<Video> findRandomPublishedVideos(Pageable pageable);



}
