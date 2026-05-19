package com.lucafu.anime_tracker_api.modules.review.controller;

import com.lucafu.anime_tracker_api.modules.review.dto.ReviewRequestDto;
import com.lucafu.anime_tracker_api.modules.review.dto.ReviewResponseDto;
import com.lucafu.anime_tracker_api.modules.review.service.ReviewService;
import com.lucafu.anime_tracker_api.modules.user.model.User;
import com.lucafu.anime_tracker_api.modules.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<ReviewResponseDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.findAll(page, size));
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDto> upsert(Authentication authentication,
                                                    @Valid @RequestBody ReviewRequestDto dto) {
        Integer userId = resolveUserId(authentication);
        return ResponseEntity.ok(reviewService.upsert(userId, dto));
    }

    @GetMapping("/anime/{animeId}")
    public ResponseEntity<List<ReviewResponseDto>> findByAnime(@PathVariable Integer animeId) {
        return ResponseEntity.ok(reviewService.findByAnime(animeId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReviewResponseDto>> findMyReviews(Authentication authentication) {
        Integer userId = resolveUserId(authentication);
        return ResponseEntity.ok(reviewService.findByUser(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDto>> findByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(reviewService.findByUser(userId));
    }

    @DeleteMapping("/{animeId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Integer animeId) {
        Integer userId = resolveUserId(authentication);
        reviewService.delete(userId, animeId);
        return ResponseEntity.noContent().build();
    }

    private Integer resolveUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getIdUser();
    }
}
