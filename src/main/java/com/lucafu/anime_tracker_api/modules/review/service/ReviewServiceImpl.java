package com.lucafu.anime_tracker_api.modules.review.service;

import com.lucafu.anime_tracker_api.modules.anime.model.Anime;
import com.lucafu.anime_tracker_api.modules.anime.repository.AnimeRepository;
import com.lucafu.anime_tracker_api.modules.review.dto.ReviewRequestDto;
import com.lucafu.anime_tracker_api.modules.review.dto.ReviewResponseDto;
import com.lucafu.anime_tracker_api.modules.review.model.Review;
import com.lucafu.anime_tracker_api.modules.review.repository.ReviewRepository;
import com.lucafu.anime_tracker_api.modules.user.model.User;
import com.lucafu.anime_tracker_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AnimeRepository animeRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponseDto upsert(Integer userId, ReviewRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Anime anime = animeRepository.findById(dto.getAnimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));

        Review review = reviewRepository
                .findByUser_IdUserAndAnime_IdAnime(userId, dto.getAnimeId())
                .orElseGet(Review::new);

        review.setUser(user);
        review.setAnime(anime);
        review.setScore(dto.getScore());

        return toDto(reviewRepository.save(review));
    }

    @Override
    public List<ReviewResponseDto> findByAnime(Integer animeId) {
        return reviewRepository.findByAnime_IdAnime(animeId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<ReviewResponseDto> findByUser(Integer userId) {
        return reviewRepository.findByUser_IdUser(userId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public void delete(Integer userId, Integer animeId) {
        Review review = reviewRepository.findByUser_IdUserAndAnime_IdAnime(userId, animeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        reviewRepository.delete(review);
    }

    private ReviewResponseDto toDto(Review review) {
        return new ReviewResponseDto(
                review.getIdReview(),
                review.getAnime().getIdAnime(),
                review.getAnime().getName(),
                review.getUser().getIdUser(),
                review.getUser().getUsername(),
                review.getScore()
        );
    }
}
