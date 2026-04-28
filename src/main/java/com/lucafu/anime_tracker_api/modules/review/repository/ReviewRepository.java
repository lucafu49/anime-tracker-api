package com.lucafu.anime_tracker_api.modules.review.repository;

import com.lucafu.anime_tracker_api.modules.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findByUser_IdUserAndAnime_IdAnime(Integer userId, Integer animeId);

    List<Review> findByAnime_IdAnime(Integer animeId);

    List<Review> findByUser_IdUser(Integer userId);

    @Query("SELECT AVG(r.score) FROM Review r WHERE r.anime.idAnime = :animeId")
    BigDecimal findAverageScoreByAnimeId(@Param("animeId") Integer animeId);
}
