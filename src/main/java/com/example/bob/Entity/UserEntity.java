package com.example.bob.Entity;

import com.example.bob.DTO.UserDTO;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class UserEntity {
    //값 1씩 증가
    //@GeneratedValue(strategy = GenerationType.IDENTITY);

    @Column(length = 100)
    private String userName;

    @Id //PK
    private String userID;

    @Column(length = 100)
    private String pwd;

    @Column(length = 100)
    private String userEmail;

    @Column(length = 14)
    private String userPhone;

    @Column(length = 2)
    private String Sex;

    @Column(length = 100)
    private String MainLanguage;

    @Column(length = 8)
    private String Birthday;

    @Builder
    public static UserEntity toUserEntity(UserDTO userDTO) {
        UserEntity userEntity = new UserEntity();

        userEntity.userID = userDTO.getUserID();
        userEntity.pwd = userDTO.getPwd();
        userEntity.userName = userDTO.getUserName();
        userEntity.userEmail = userDTO.getUserEmail();
        userEntity.userPhone = userDTO.getUserPhone();
        userEntity.Sex = userDTO.getSex();
        userEntity.MainLanguage = userDTO.getMainLanguage();
        userEntity.Birthday = userDTO.getBirthday();

        return userEntity;
    }
}
