package com.lucafu.anime_tracker_api.modules.anime.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AnimeResponseDto {

    private Integer idAnime;
    private String name;
    private String imageUrl;
    private Boolean classic;
    private BigDecimal averageScore;
    private Integer ratingCount;
    private BigDecimal userScore;
}
