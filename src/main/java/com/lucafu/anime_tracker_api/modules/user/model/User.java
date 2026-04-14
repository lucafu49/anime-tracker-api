package com.lucafu.anime_tracker_api.modules.user.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDUser")
    private Integer idUser;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "HashedPass", nullable = false, length = 255)
    private String hashedPass;
}
