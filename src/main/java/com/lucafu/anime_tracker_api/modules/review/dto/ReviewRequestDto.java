package com.lucafu.anime_tracker_api.modules.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReviewRequestDto {

    @NotNull(message = "Anime ID is required")
    private Integer animeId;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be at least 0")
    @DecimalMax(value = "5.0", message = "Score must be at most 5")
    private BigDecimal score;
}
