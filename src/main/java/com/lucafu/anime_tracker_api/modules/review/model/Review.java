package com.lucafu.anime_tracker_api.modules.review.model;

import com.lucafu.anime_tracker_api.modules.anime.model.Anime;
import com.lucafu.anime_tracker_api.modules.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDReview")
    private Integer idReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUser", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDAnime", nullable = false, foreignKey = @ForeignKey(name = "FK_Review_Anime"))
    private Anime anime;

    @Column(name = "Score", nullable = false, precision = 3, scale = 1)
    private java.math.BigDecimal score;

    @UpdateTimestamp
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}
