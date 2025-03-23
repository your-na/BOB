package com.example.bob.DTO;

import com.example.bob.Entity.UserEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long userId;
    private String userName;        // 이름
    private String userIdLogin;          // 아이디
    private String pwd;             // 비밀번호
    private String userEmail;       // 이메일
    private String userPhone;       // 전화번호
    private String sex;             // 성별
    private String mainLanguage;    // 주 언어
    private String birthday;        // 생년월일
    private String userNick;        // 유저 닉네임
    private LocalDateTime accountCreatedAt; // 계정 생성 날짜
    private String profileImageUrl;
    private String bio;

    // UserDTO(Long, String) 생성자 명시적으로 추가
    public UserDTO(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public static UserDTO toUserDTO(UserEntity userEntity) {
        UserDTO userDTO = new UserDTO();

        userDTO.setUserId(userEntity.getUserId());
        userDTO.setUserName(userEntity.getUserName());
        userDTO.setUserIdLogin(userEntity.getUserIdLogin());
        userDTO.setPwd(userEntity.getPwd());
        userDTO.setUserEmail(userEntity.getUserEmail());
        userDTO.setUserPhone(userEntity.getUserPhone());
        userDTO.setSex(userEntity.getSex());
        userDTO.setMainLanguage(userEntity.getMainLanguage());
        userDTO.setBirthday(userEntity.getBirthday());
        userDTO.setUserNick(userEntity.getUserNick());
        userDTO.setProfileImageUrl(userEntity.getProfileImageUrl() != null ? userEntity.getProfileImageUrl() : "/images/user.png");
        userDTO.setBio(userEntity.getUserBio() != null ? userEntity.getUserBio() : "소개를 작성해보세요.");
        userDTO.setAccountCreatedAt(userEntity.getAccountCreatedAt() != null ? userEntity.getAccountCreatedAt() : LocalDateTime.now());

        return userDTO;
    }

    public UserEntity toUserEntity() {
        return UserEntity.toUserEntity(this);
    }

    public static UserDTO fromEntity(UserEntity userEntity) {
        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .userName(userEntity.getUserName())
                .userIdLogin(userEntity.getUserIdLogin())
                .pwd(userEntity.getPwd())
                .userEmail(userEntity.getUserEmail())
                .userPhone(userEntity.getUserPhone())
                .sex(userEntity.getSex())
                .mainLanguage(userEntity.getMainLanguage())
                .birthday(userEntity.getBirthday())
                .userNick(userEntity.getUserNick())
                .profileImageUrl(userEntity.getProfileImageUrl() != null ? userEntity.getProfileImageUrl() : "/images/user.png")
                .bio(userEntity.getUserBio() != null ? userEntity.getUserBio() : "소개를 작성해보세요.")
                .accountCreatedAt(userEntity.getAccountCreatedAt() != null ? userEntity.getAccountCreatedAt() : LocalDateTime.now())
                .build();
    }

}
