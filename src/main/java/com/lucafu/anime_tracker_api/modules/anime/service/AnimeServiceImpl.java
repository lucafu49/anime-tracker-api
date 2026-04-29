package com.lucafu.anime_tracker_api.modules.anime.service;

import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeRequestDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeResponseDto;
import com.lucafu.anime_tracker_api.modules.anime.model.Anime;
import com.lucafu.anime_tracker_api.modules.anime.repository.AnimeRepository;
import com.lucafu.anime_tracker_api.modules.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AnimeServiceImpl implements AnimeService {

    private final AnimeRepository animeRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public Page<AnimeResponseDto> findAll(String name, Boolean classic, String sort, int pageNumber, Integer userId, Boolean unreviewed) {
        boolean hasName = name != null && !name.isBlank();

        Page<Anime> animePage;
        if (Boolean.TRUE.equals(unreviewed)) {
            if ("score".equals(sort)) {
                animePage = animeRepository.findUnreviewedSortedByAverageScore(
                        hasName ? name : null, classic, userId, PageRequest.of(pageNumber, 12));
            } else {
                PageRequest pageable = "name".equals(sort)
                        ? PageRequest.of(pageNumber, 12, Sort.by(Sort.Direction.ASC, "name"))
                        : PageRequest.of(pageNumber, 12);
                animePage = animeRepository.findUnreviewed(hasName ? name : null, classic, userId, pageable);
            }
        } else if ("score".equals(sort)) {
            animePage = animeRepository.findAllSortedByAverageScore(hasName ? name : null, classic, PageRequest.of(pageNumber, 12));
        } else {
            PageRequest pageable = "name".equals(sort)
                    ? PageRequest.of(pageNumber, 12, Sort.by(Sort.Direction.ASC, "name"))
                    : PageRequest.of(pageNumber, 12);
            boolean hasClassic = classic != null;
            if (hasName && hasClassic) {
                animePage = animeRepository.findByNameContainingIgnoreCaseAndClassic(name, classic, pageable);
            } else if (hasName) {
                animePage = animeRepository.findByNameContainingIgnoreCase(name, pageable);
            } else if (hasClassic) {
                animePage = animeRepository.findByClassic(classic, pageable);
            } else {
                animePage = animeRepository.findAll(pageable);
            }
        }

        List<Integer> animeIds = animePage.getContent().stream().map(Anime::getIdAnime).toList();
        Map<Integer, BigDecimal> userScores = reviewRepository
                .findByUser_IdUserAndAnime_IdAnimeIn(userId, animeIds)
                .stream()
                .collect(Collectors.toMap(r -> r.getAnime().getIdAnime(), r -> r.getScore()));

        return animePage.map(anime -> toDto(anime, userScores.get(anime.getIdAnime())));
    }

    @Override
    public AnimeResponseDto findById(Integer id, Integer userId) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
        BigDecimal userScore = reviewRepository.findByUser_IdUserAndAnime_IdAnime(userId, id)
                .map(r -> r.getScore())
                .orElse(null);
        return toDto(anime, userScore);
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
        return toDto(anime, null);
    }

    private AnimeResponseDto toDto(Anime anime, BigDecimal userScore) {
        return new AnimeResponseDto(
                anime.getIdAnime(),
                anime.getName(),
                anime.getImageUrl(),
                anime.getClassic(),
                reviewRepository.findAverageScoreByAnimeId(anime.getIdAnime()),
                userScore
        );
    }
}
