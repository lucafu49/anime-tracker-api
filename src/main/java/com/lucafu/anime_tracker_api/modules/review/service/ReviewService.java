package com.lucafu.anime_tracker_api.modules.review.service;

import com.lucafu.anime_tracker_api.modules.review.dto.ReviewRequestDto;
import com.lucafu.anime_tracker_api.modules.review.dto.ReviewResponseDto;

import java.util.List;

public interface ReviewService {

    ReviewResponseDto upsert(Integer userId, ReviewRequestDto dto);

    List<ReviewResponseDto> findByAnime(Integer animeId);

    List<ReviewResponseDto> findByUser(Integer userId);

    void delete(Integer userId, Integer animeId);
}
