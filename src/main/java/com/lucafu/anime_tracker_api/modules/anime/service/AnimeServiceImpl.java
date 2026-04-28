package com.lucafu.anime_tracker_api.modules.anime.service;

import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeRequestDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeResponseDto;
import com.lucafu.anime_tracker_api.modules.anime.model.Anime;
import com.lucafu.anime_tracker_api.modules.anime.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimeServiceImpl implements AnimeService {

    private final AnimeRepository animeRepository;

    @Override
    public List<AnimeResponseDto> findAll(String name) {
        List<Anime> results = (name != null && !name.isBlank())
                ? animeRepository.findByNameContainingIgnoreCase(name)
                : animeRepository.findAll();
        return results.stream().map(this::toDto).toList();
    }

    @Override
    public AnimeResponseDto findById(Integer id) {
        return animeRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

    @Override
    public AnimeResponseDto create(AnimeRequestDto dto) {
        Anime anime = new Anime();
        anime.setName(dto.getName());
        anime.setImageUrl(dto.getImageUrl());
        anime.setClassic(dto.getClassic());
        return toDto(animeRepository.save(anime));
    }

    @Override
    public AnimeResponseDto update(Integer id, AnimeRequestDto dto) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
        anime.setName(dto.getName());
        anime.setImageUrl(dto.getImageUrl());
        anime.setClassic(dto.getClassic());
        return toDto(animeRepository.save(anime));
    }

    @Override
    public void delete(Integer id) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
        animeRepository.delete(anime);
    }

    private AnimeResponseDto toDto(Anime anime) {
        return new AnimeResponseDto(anime.getIdAnime(), anime.getName(), anime.getImageUrl(), anime.getClassic());
    }
}
