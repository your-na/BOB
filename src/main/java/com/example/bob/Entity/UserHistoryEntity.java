package com.example.bob.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_history")
public class UserHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity; // 사용자 엔티티와 연결

    @Column(length = 100)
    private String userNick;

    @Column(length = 100)
    private String userEmail;

    @Column(length = 500)
    private String userBio;

    @Column(length = 100)
    private String mainLanguage;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserHistoryEntity(UserEntity userEntity, String userNick, String userEmail, String userBio, String mainLanguage, LocalDateTime updatedAt) {
        this.userEntity = userEntity;
        this.userNick = userNick;
        this.userEmail = userEmail;
        this.userBio = userBio;
        this.mainLanguage = mainLanguage;
        this.updatedAt = updatedAt;
    }
}

