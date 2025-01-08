package com.example.bob.Entity;

import com.example.bob.DTO.UserDTO;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

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

    @Builder
    public static UserEntity toUserEntity(UserDTO userDTO) {
        UserEntity userEntity = new UserEntity();

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

        return userEntity;
    }
}
