package com.lucafu.anime_tracker_api.modules.anime.controller;

import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeRequestDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeResponseDto;
import com.lucafu.anime_tracker_api.modules.anime.service.AnimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/animes")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeService animeService;

    @GetMapping
    public ResponseEntity<List<AnimeResponseDto>> findAll() {
        return ResponseEntity.ok(animeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimeResponseDto> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(animeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AnimeResponseDto> create(@RequestBody AnimeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimeResponseDto> update(@PathVariable Integer id, @RequestBody AnimeRequestDto dto) {
        return ResponseEntity.ok(animeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        animeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
