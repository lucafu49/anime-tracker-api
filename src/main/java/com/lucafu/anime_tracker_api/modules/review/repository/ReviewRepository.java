package com.lucafu.anime_tracker_api.modules.review.repository;

import com.lucafu.anime_tracker_api.modules.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findByUser_IdUserAndAnime_IdAnime(Integer userId, Integer animeId);

    List<Review> findByAnime_IdAnime(Integer animeId);

    List<Review> findByUser_IdUser(Integer userId);

    List<Review> findByUser_IdUserAndAnime_IdAnimeIn(Integer userId, Collection<Integer> animeIds);

    @Query("SELECT AVG(r.score) FROM Review r WHERE r.anime.idAnime = :animeId")
    BigDecimal findAverageScoreByAnimeId(@Param("animeId") Integer animeId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.anime.idAnime = :animeId")
    Long findRatingCountByAnimeId(@Param("animeId") Integer animeId);

    @Query("SELECT r.anime.idAnime, AVG(r.score) FROM Review r WHERE r.anime.idAnime IN :animeIds GROUP BY r.anime.idAnime")
    List<Object[]> findAverageScoresByAnimeIds(@Param("animeIds") Collection<Integer> animeIds);

    @Query("SELECT r.anime.idAnime, COUNT(r) FROM Review r WHERE r.anime.idAnime IN :animeIds GROUP BY r.anime.idAnime")
    List<Object[]> findRatingCountsByAnimeIds(@Param("animeIds") Collection<Integer> animeIds);

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
