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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AnimeRepository animeRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Page<ReviewResponseDto> findAll(int page, int size) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::toDto);
    }

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

        ReviewResponseDto saved = toDto(reviewRepository.save(review));

        // Broadcast a todos los suscriptores del feed global y del anime específico
        messagingTemplate.convertAndSend("/topic/reviews", saved);
        messagingTemplate.convertAndSend("/topic/reviews/" + saved.getAnimeId(), saved);

        return saved;
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

        // Broadcast del borrado: el feed global y el anime específico
        Map<String, Integer> payload = Map.of("userId", userId, "animeId", animeId);
        messagingTemplate.convertAndSend("/topic/reviews/deleted", payload);
        messagingTemplate.convertAndSend("/topic/reviews/deleted/" + animeId, payload);
    }

    private ReviewResponseDto toDto(Review review) {
        return new ReviewResponseDto(
                review.getIdReview(),
                review.getAnime().getIdAnime(),
                review.getAnime().getName(),
                review.getAnime().getImageUrl(),
                review.getUser().getIdUser(),
                review.getUser().getUsername(),
                review.getScore(),
                review.getCreatedAt(),
                List.copyOf(review.getAnime().getGenres())
        );
    }
}
