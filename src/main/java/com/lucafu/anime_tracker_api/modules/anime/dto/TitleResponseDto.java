package com.lucafu.anime_tracker_api.modules.anime.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class TitleResponseDto {

    private Integer idAnime;
    private String name;
    private String imageUrl;
    private Boolean classic;
    private BigDecimal circleAverageScore;
    private Integer circleRatingCount;
    private Integer totalCircleSize;
    private List<CircleMemberRatingDto> ratings;
}
