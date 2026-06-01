package com.lucafu.anime_tracker_api.modules.anime.service;

import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeRequestDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeResponseDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.CircleMemberRatingDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.TitleResponseDto;
import com.lucafu.anime_tracker_api.modules.anime.model.Anime;
import com.lucafu.anime_tracker_api.modules.anime.repository.AnimeRepository;
import com.lucafu.anime_tracker_api.modules.review.model.Review;
import com.lucafu.anime_tracker_api.modules.review.repository.ReviewRepository;
import com.lucafu.anime_tracker_api.modules.user.model.User;
import com.lucafu.anime_tracker_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final UserRepository userRepository;

    // Constante "m" del promedio bayesiano usado para ordenar "Mejores Puntuados":
    // pondera el promedio de cada anime contra la media global según su cantidad de votos.
    // Valor bajo = penaliza poco a los animes con pocos votos.
    private static final int BAYESIAN_MIN_VOTES = 2;

    @Override
    public List<TitleResponseDto> findTitles() {
        List<Anime> animes = animeRepository.findAnimesWithAtLeastOneReview();
        List<Integer> animeIds = animes.stream().map(Anime::getIdAnime).toList();
        List<User> allUsers = userRepository.findAll();
        List<Review> reviews = reviewRepository.findByAnime_IdAnimeIn(animeIds);

        Map<Integer, List<Review>> reviewsByAnime = reviews.stream()
                .collect(Collectors.groupingBy(r -> r.getAnime().getIdAnime()));

        int totalCircleSize = allUsers.size();

        return animes.stream().map(anime -> {
            List<Review> animeReviews = reviewsByAnime.getOrDefault(anime.getIdAnime(), List.of());

            Map<Integer, BigDecimal> scoreByUserId = animeReviews.stream()
                    .collect(Collectors.toMap(r -> r.getUser().getIdUser(), Review::getScore));

            BigDecimal avg = animeReviews.isEmpty() ? null :
                    animeReviews.stream()
                            .map(Review::getScore)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(animeReviews.size()), 2, RoundingMode.HALF_UP);

            List<CircleMemberRatingDto> ratings = allUsers.stream()
                    .map(u -> new CircleMemberRatingDto(u.getIdUser(), u.getUsername(), scoreByUserId.get(u.getIdUser())))
                    .toList();

            return new TitleResponseDto(
                    anime.getIdAnime(),
                    anime.getName(),
                    anime.getImageUrl(),
                    anime.getClassic(),
                    avg,
                    animeReviews.size(),
                    totalCircleSize,
                    ratings
            );
        }).toList();
    }

    @Override
    public Page<AnimeResponseDto> findAll(String name, Boolean classic, String sort, int pageNumber, Integer userId, Boolean unreviewed, Integer reviewedBy) {
        boolean hasName = name != null && !name.isBlank();

        Page<Anime> animePage;
        if (reviewedBy != null) {
            if ("score".equals(sort)) {
                animePage = animeRepository.findReviewedBySortedByAverageScore(
                        reviewedBy, hasName ? name : null, classic, PageRequest.of(pageNumber, 12));
            } else {
                PageRequest pageable = "name".equals(sort)
                        ? PageRequest.of(pageNumber, 12, Sort.by(Sort.Direction.ASC, "name"))
                        : PageRequest.of(pageNumber, 12);
                animePage = animeRepository.findReviewedBy(reviewedBy, hasName ? name : null, classic, pageable);
            }
        } else if (Boolean.TRUE.equals(unreviewed)) {
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
            animePage = animeRepository.findAllSortedByAverageScore(hasName ? name : null, classic, BAYESIAN_MIN_VOTES, PageRequest.of(pageNumber, 12));
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

        Map<Integer, BigDecimal> averageScores = reviewRepository
                .findAverageScoresByAnimeIds(animeIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).intValue(),
                        r -> r[1] != null ? BigDecimal.valueOf(((Number) r[1]).doubleValue()) : null));

        Map<Integer, Integer> ratingCounts = reviewRepository
                .findRatingCountsByAnimeIds(animeIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).intValue(),
                        r -> ((Number) r[1]).intValue()));

        return animePage.map(anime -> toDto(
                anime,
                userScores.get(anime.getIdAnime()),
                averageScores.get(anime.getIdAnime()),
                ratingCounts.getOrDefault(anime.getIdAnime(), 0)));
    }

    @Override
    public AnimeResponseDto findById(Integer id, Integer userId) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
        BigDecimal userScore = reviewRepository.findByUser_IdUserAndAnime_IdAnime(userId, id)
                .map(r -> r.getScore())
                .orElse(null);
        BigDecimal averageScore = reviewRepository.findAverageScoreByAnimeId(id);
        Long count = reviewRepository.findRatingCountByAnimeId(id);
        return toDto(anime, userScore, averageScore, count != null ? count.intValue() : 0);
    }

    @Override
    public AnimeResponseDto create(AnimeRequestDto dto) {
        Anime anime = new Anime();
        anime.setName(dto.getName());
        anime.setImageUrl(dto.getImageUrl());
        anime.setClassic(dto.getClassic());
        return toDto(animeRepository.save(anime), null, null, 0);
    }

    @Override
    public AnimeResponseDto update(Integer id, AnimeRequestDto dto) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
        anime.setName(dto.getName());
        anime.setImageUrl(dto.getImageUrl());
        anime.setClassic(dto.getClassic());
        Long count = reviewRepository.findRatingCountByAnimeId(id);
        return toDto(animeRepository.save(anime), null, reviewRepository.findAverageScoreByAnimeId(id), count != null ? count.intValue() : 0);
    }

    @Override
    public void delete(Integer id) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
        reviewRepository.deleteAll(reviewRepository.findByAnime_IdAnime(id));
        animeRepository.delete(anime);
    }

    private AnimeResponseDto toDto(Anime anime, BigDecimal userScore, BigDecimal averageScore, Integer ratingCount) {
        return new AnimeResponseDto(
                anime.getIdAnime(),
                anime.getName(),
                anime.getImageUrl(),
                anime.getClassic(),
                averageScore,
                ratingCount,
                userScore
        );
    }
}
