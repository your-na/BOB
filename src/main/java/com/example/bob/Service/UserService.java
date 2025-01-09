package com.example.bob.Service;

import com.example.bob.DTO.UserDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder; //비밀번호 암호화
    private final UserRepository userRepository; //jpa, MySql, dependency 추가

    // 회원가입 처리
    public void save(UserDTO userDTO) {
        // 비밀번호 암호화
        userDTO.setPwd(passwordEncoder.encode(userDTO.getPwd()));

        // request -> DTO -> Entity -> Repository에서 save
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);

        // 사용자 저장
        userRepository.save(userEntity);
    }

    // 로그인 처리
    public boolean login(String userIdLogin, String password) {
        Optional<UserEntity> userOpt = userRepository.findByUserIdLogin(userIdLogin);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            // 비밀번호 확인
            return passwordEncoder.matches(password, user.getPwd());
        }
        return false;
    }
}
