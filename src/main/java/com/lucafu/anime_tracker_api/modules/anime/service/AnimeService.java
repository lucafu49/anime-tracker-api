package com.lucafu.anime_tracker_api.modules.anime.service;

import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeRequestDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeResponseDto;
import org.springframework.data.domain.Page;

public interface AnimeService {

    Page<AnimeResponseDto> findAll(String name, Boolean classic, String sort, int page, Integer userId);

    AnimeResponseDto findById(Integer id, Integer userId);

    AnimeResponseDto create(AnimeRequestDto dto);

    AnimeResponseDto update(Integer id, AnimeRequestDto dto);

    void delete(Integer id);
}
