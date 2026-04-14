package com.lucafu.anime_tracker_api.modules.anime.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimeRequestDto {

    private String name;
    private String imageUrl;
    private Boolean classic;
}
