package com.lucafu.anime_tracker_api.modules.anime.repository;

import com.lucafu.anime_tracker_api.modules.anime.model.Anime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimeRepository extends JpaRepository<Anime, Integer> {

    List<Anime> findByNameContainingIgnoreCase(String name);
}
