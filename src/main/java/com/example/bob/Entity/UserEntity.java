package com.example.bob.Entity;

import com.example.bob.DTO.UserDTO;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class UserEntity {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 증가 id
    private Long userId;

    @Column(length = 100, unique = true)
    private String userNick;

    @Column(length = 100, unique = true) // 아이디는 유일한 값이므로 unique 제약 추가
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
    private String MainLanguage;

    @Column(length = 100)
    private String Birthday;

    @Column(length = 255)
    private String profileImageUrl = "/images/user.png"; //기본 이미지 경로

    @Column(name = "account_created_at")
    private LocalDateTime accountCreatedAt; // 계정 생성 날짜

    @Column(length = 500)
    private String userBio = "소개를 작성해보세요.";

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHistoryEntity> userHistories = new ArrayList<>();

    @Builder
    public static UserEntity toUserEntity(UserDTO userDTO) {
        UserEntity userEntity = new UserEntity();

        userEntity.userId = userDTO.getUserId();
        // userID는 자동으로 설정되므로 DTO에서 받아오지 않습니다.
        userEntity.userNick = userDTO.getUserNick();
        userEntity.userName = userDTO.getUserName();
        userEntity.userIdLogin = userDTO.getUserIdLogin();
        userEntity.pwd = userDTO.getPwd();
        userEntity.userEmail = userDTO.getUserEmail();
        userEntity.userPhone = userDTO.getUserPhone();
        userEntity.sex = userDTO.getSex();
        userEntity.MainLanguage = userDTO.getMainLanguage();
        userEntity.Birthday = userDTO.getBirthday();
        userEntity.profileImageUrl = (userDTO.getProfileImageUrl() != null) ? userDTO.getProfileImageUrl() : "/images/user.png";
        userEntity.userBio = (userDTO.getBio() != null) ? userDTO.getBio() : "소개를 작성해보세요.";
        userEntity.accountCreatedAt = (userDTO.getAccountCreatedAt() != null) ? userDTO.getAccountCreatedAt() : LocalDateTime.now();

        return userEntity;
    }

    //프로필 이미지 URL을 설정하는 메서드
    public void setProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // UserHistory 추가
    public void addHistory(UserHistoryEntity history) {
        this.userHistories.add(history);
    }
}