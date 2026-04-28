package com.lucafu.anime_tracker_api.modules.anime.repository;

import com.lucafu.anime_tracker_api.modules.anime.model.Anime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimeRepository extends JpaRepository<Anime, Integer> {

    Page<Anime> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Anime> findByClassic(Boolean classic, Pageable pageable);

    Page<Anime> findByNameContainingIgnoreCaseAndClassic(String name, Boolean classic, Pageable pageable);

    @Query(value = """
            SELECT a.IDAnime, a.Name, a.ImageURL, a.Classic
            FROM Anime a
            LEFT JOIN Review r ON r.IDAnime = a.IDAnime
            WHERE (:name IS NULL OR a.Name LIKE CONCAT('%', :name, '%'))
            AND (:classic IS NULL OR a.Classic = :classic)
            GROUP BY a.IDAnime, a.Name, a.ImageURL, a.Classic
            ORDER BY COALESCE(AVG(r.Score), -1) DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
                SELECT a.IDAnime FROM Anime a
                WHERE (:name IS NULL OR a.Name LIKE CONCAT('%', :name, '%'))
                AND (:classic IS NULL OR a.Classic = :classic)
            ) sub
            """,
            nativeQuery = true)
    Page<Anime> findAllSortedByAverageScore(@Param("name") String name, @Param("classic") Boolean classic, Pageable pageable);
}
