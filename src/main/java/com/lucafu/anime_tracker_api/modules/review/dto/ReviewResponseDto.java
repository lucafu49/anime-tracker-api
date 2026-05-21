package com.lucafu.anime_tracker_api.modules.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReviewResponseDto {

    private Integer idReview;
    private Integer animeId;
    private String animeName;
    private String animeImageUrl;
    private Integer userId;
    private String username;
    private BigDecimal score;
    private LocalDateTime createdAt;
}
