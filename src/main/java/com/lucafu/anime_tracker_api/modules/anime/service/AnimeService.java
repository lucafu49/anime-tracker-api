package com.lucafu.anime_tracker_api.modules.anime.service;

import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeRequestDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeResponseDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.TitleResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AnimeService {

    List<TitleResponseDto> findTitles();

    Page<AnimeResponseDto> findAll(String name, Boolean classic, String sort, int page, Integer userId, Boolean unreviewed);

    AnimeResponseDto findById(Integer id, Integer userId);

    AnimeResponseDto create(AnimeRequestDto dto);

    AnimeResponseDto update(Integer id, AnimeRequestDto dto);

    void delete(Integer id);
}
