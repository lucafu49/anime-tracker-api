package com.lucafu.anime_tracker_api.modules.anime.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class AnimeRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must be at most 500 characters")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    @NotNull(message = "Classic field is required")
    private Boolean classic;
}
