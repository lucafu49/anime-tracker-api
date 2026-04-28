package com.lucafu.anime_tracker_api.modules.anime.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}
