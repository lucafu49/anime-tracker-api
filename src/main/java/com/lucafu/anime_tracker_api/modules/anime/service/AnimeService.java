package com.lucafu.anime_tracker_api.modules.anime.service;

import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeRequestDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeResponseDto;

import java.util.List;

public interface AnimeService {

    List<AnimeResponseDto> findAll(String name);

    AnimeResponseDto findById(Integer id);

    AnimeResponseDto create(AnimeRequestDto dto);

    AnimeResponseDto update(Integer id, AnimeRequestDto dto);

    void delete(Integer id);
}
