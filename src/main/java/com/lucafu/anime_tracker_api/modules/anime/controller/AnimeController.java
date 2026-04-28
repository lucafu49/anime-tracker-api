package com.lucafu.anime_tracker_api.modules.anime.controller;

import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeRequestDto;
import com.lucafu.anime_tracker_api.modules.anime.dto.AnimeResponseDto;
import com.lucafu.anime_tracker_api.modules.anime.service.AnimeService;
import com.lucafu.anime_tracker_api.modules.user.model.User;
import com.lucafu.anime_tracker_api.modules.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/animes")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeService animeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<AnimeResponseDto>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean classic,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication) {
        Integer userId = resolveUserId(authentication);
        return ResponseEntity.ok(animeService.findAll(name, classic, sort, page, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimeResponseDto> findById(@PathVariable Integer id, Authentication authentication) {
        Integer userId = resolveUserId(authentication);
        return ResponseEntity.ok(animeService.findById(id, userId));
    }

    private Integer resolveUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getIdUser();
    }

    @PostMapping
    public ResponseEntity<AnimeResponseDto> create(@Valid @RequestBody AnimeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimeResponseDto> update(@PathVariable Integer id, @Valid @RequestBody AnimeRequestDto dto) {
        return ResponseEntity.ok(animeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        animeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
