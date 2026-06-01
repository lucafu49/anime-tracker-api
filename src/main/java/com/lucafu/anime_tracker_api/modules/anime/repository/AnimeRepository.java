package com.lucafu.anime_tracker_api.modules.anime.repository;

import com.lucafu.anime_tracker_api.modules.anime.model.Anime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimeRepository extends JpaRepository<Anime, Integer> {

    @Query("SELECT DISTINCT a FROM Anime a WHERE EXISTS (SELECT r FROM Review r WHERE r.anime = a)")
    List<Anime> findAnimesWithAtLeastOneReview();

    Page<Anime> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Anime> findByClassic(Boolean classic, Pageable pageable);

    Page<Anime> findByNameContainingIgnoreCaseAndClassic(String name, Boolean classic, Pageable pageable);

    @Query(value = """
            SELECT a.IDAnime, a.Name, a.ImageURL, a.Classic
            FROM Anime a
            LEFT JOIN Review r ON r.IDAnime = a.IDAnime
            WHERE (:name IS NULL OR a.Name ILIKE CONCAT('%', :name, '%'))
            AND (:classic IS NULL OR a.Classic = :classic)
            GROUP BY a.IDAnime, a.Name, a.ImageURL, a.Classic
            ORDER BY
              CASE WHEN COUNT(r.Score) = 0 THEN -1
              ELSE (
                (CAST(COUNT(r.Score) AS numeric) / (COUNT(r.Score) + :minVotes)) * AVG(r.Score)
                + (CAST(:minVotes AS numeric) / (COUNT(r.Score) + :minVotes)) * (SELECT AVG(Score) FROM Review)
              )
              END DESC,
              COUNT(r.Score) DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT a.IDAnime FROM Anime a
                WHERE (:name IS NULL OR a.Name ILIKE CONCAT('%', :name, '%'))
                AND (:classic IS NULL OR a.Classic = :classic)
            ) sub
            """,
            nativeQuery = true)
    Page<Anime> findAllSortedByAverageScore(@Param("name") String name, @Param("classic") Boolean classic,
                                            @Param("minVotes") int minVotes, Pageable pageable);

    @Query("""
            SELECT a FROM Anime a
            WHERE (:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:classic IS NULL OR a.classic = :classic)
            AND NOT EXISTS (
                SELECT r FROM Review r WHERE r.anime = a AND r.user.idUser = :userId
            )
            """)
    Page<Anime> findUnreviewed(@Param("name") String name, @Param("classic") Boolean classic,
                               @Param("userId") Integer userId, Pageable pageable);

    @Query(value = """
            SELECT a.IDAnime, a.Name, a.ImageURL, a.Classic
            FROM Anime a
            LEFT JOIN Review r ON r.IDAnime = a.IDAnime
            WHERE (:name IS NULL OR a.Name ILIKE CONCAT('%', :name, '%'))
            AND (:classic IS NULL OR a.Classic = :classic)
            AND NOT EXISTS (
                SELECT 1 FROM Review r2 WHERE r2.IDAnime = a.IDAnime AND r2.IDUser = :userId
            )
            GROUP BY a.IDAnime, a.Name, a.ImageURL, a.Classic
            ORDER BY COALESCE(AVG(r.Score), -1) DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT a.IDAnime FROM Anime a
                WHERE (:name IS NULL OR a.Name ILIKE CONCAT('%', :name, '%'))
                AND (:classic IS NULL OR a.Classic = :classic)
                AND NOT EXISTS (
                    SELECT 1 FROM Review r2 WHERE r2.IDAnime = a.IDAnime AND r2.IDUser = :userId
                )
            ) sub
            """,
            nativeQuery = true)
    Page<Anime> findUnreviewedSortedByAverageScore(@Param("name") String name, @Param("classic") Boolean classic,
                                                   @Param("userId") Integer userId, Pageable pageable);

    @Query("""
            SELECT a FROM Anime a
            WHERE EXISTS (SELECT r FROM Review r WHERE r.anime = a AND r.user.idUser = :reviewedBy)
            AND (:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:classic IS NULL OR a.classic = :classic)
            """)
    Page<Anime> findReviewedBy(@Param("reviewedBy") Integer reviewedBy,
                               @Param("name") String name,
                               @Param("classic") Boolean classic,
                               Pageable pageable);

    @Query(value = """
            SELECT a.IDAnime, a.Name, a.ImageURL, a.Classic
            FROM Anime a
            LEFT JOIN Review r ON r.IDAnime = a.IDAnime
            WHERE EXISTS (SELECT 1 FROM Review r2 WHERE r2.IDAnime = a.IDAnime AND r2.IDUser = :reviewedBy)
            AND (:name IS NULL OR a.Name ILIKE CONCAT('%', :name, '%'))
            AND (:classic IS NULL OR a.Classic = :classic)
            GROUP BY a.IDAnime, a.Name, a.ImageURL, a.Classic
            ORDER BY COALESCE(AVG(r.Score), -1) DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT a.IDAnime FROM Anime a
                WHERE EXISTS (SELECT 1 FROM Review r2 WHERE r2.IDAnime = a.IDAnime AND r2.IDUser = :reviewedBy)
                AND (:name IS NULL OR a.Name ILIKE CONCAT('%', :name, '%'))
                AND (:classic IS NULL OR a.Classic = :classic)
            ) sub
            """,
            nativeQuery = true)
    Page<Anime> findReviewedBySortedByAverageScore(@Param("reviewedBy") Integer reviewedBy,
                                                   @Param("name") String name,
                                                   @Param("classic") Boolean classic,
                                                   Pageable pageable);
}
