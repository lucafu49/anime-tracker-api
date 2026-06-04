package com.lucafu.anime_tracker_api.modules.anime.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Anime")
public class Anime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDAnime")
    private Integer idAnime;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "ImageURL", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "Classic", nullable = false)
    private Boolean classic;

    // Nullable: animes existentes no tienen año registrado
    @Column(name = "Year")
    private Integer year;

    // La carga en lote de los géneros (evita N+1 en los feeds) la maneja el setting
    // global hibernate.default_batch_fetch_size en application.properties.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "AnimeGenre", joinColumns = @JoinColumn(name = "IDAnime"))
    @Column(name = "Genre", length = 40)
    private Set<String> genres = new HashSet<>();
}
