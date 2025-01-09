package com.example.bob.Repository;

import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository //객체타입, PK타입
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 회원가입
    boolean existsByUserIdLogin(String userIdLogin); // 아이디 중복 체크

    // 로그인
    Optional<UserEntity> findByUserIdLogin(String userIdLogin); // 로그인 아이디로 사용자 조회

    boolean existsByUserNick(String userNick);
}
