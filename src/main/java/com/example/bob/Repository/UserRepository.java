package com.example.bob.Repository;

import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository //객체타입, PK타입
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 회원가입
    boolean existsByUserIdLogin(String userIdLogin); // 아이디 중복 체크

    // 로그인
    Optional<UserEntity> findByUserIdLogin(String userIdLogin); // 로그인 아이디로 사용자 조회

    boolean existsByUserNick(String userNick);

    Optional<UserEntity> findByUserNick(String userNick);  // 추가된 메서드

    List<UserEntity> findByUserNickContainingIgnoreCaseOrUserIdLoginContainingIgnoreCase(String userNick, String userIdLogin);

    // 일반 회원 수 조회 (role 컬럼이 'USER'인 회원 수)
    long countByRole(String role);

    // 최근 특정 기간 내 로그인한 일반 회원 수
    long countByRoleAndLastLoginAtAfter(String role, LocalDateTime since);




}
