package com.example.bob.Repository;

import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository //객체타입, PK타입
public interface UserRepository extends JpaRepository<UserEntity, String> {
    boolean existsById(String userIdLogin); // 아이디 중복 체크

}
