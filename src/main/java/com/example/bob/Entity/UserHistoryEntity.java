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
    private String userIdLogin;

    @Column(length = 100)
    private String userName;

    @Column(length = 100)
    private String pwd;

    @Column(length = 100)
    private String userEmail;

    @Column(length = 100)
    private String userPhone;

    @Column(length = 100)
    private String sex;

    @Column(length = 100)
    private String mainLanguage;

    @Column(length = 100)
    private String birthday;

    @Column(length = 255)
    private String profileImageUrl;

    @Column(name = "account_created_at")
    private LocalDateTime accountCreatedAt; // 계정 생성 날짜

    @Column(length = 500)
    private String userBio;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserHistoryEntity(UserEntity userEntity, String userNick, String userIdLogin, String userName, String pwd,
                             String userEmail, String userPhone, String sex, String mainLanguage, String birthday,
                             String profileImageUrl, LocalDateTime accountCreatedAt, String userBio, LocalDateTime updatedAt) {
        this.userEntity = userEntity;
        this.userNick = userNick;
        this.userIdLogin = userIdLogin;
        this.userName = userName;
        this.pwd = pwd;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.sex = sex;
        this.mainLanguage = mainLanguage;
        this.birthday = birthday;
        this.profileImageUrl = profileImageUrl;
        this.accountCreatedAt = accountCreatedAt;
        this.userBio = userBio;
        this.updatedAt = updatedAt;
    }
}
