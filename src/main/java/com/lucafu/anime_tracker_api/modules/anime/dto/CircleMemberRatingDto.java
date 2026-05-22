package com.lucafu.anime_tracker_api.modules.anime.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CircleMemberRatingDto {

    private Integer userId;
    private String username;
    private BigDecimal score;
}
