package com.example.bob.Repository;

import com.example.bob.Entity.UserHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistoryEntity, Long> {
    List<UserHistoryEntity> findByUserEntityUserId(Long userId); // userId를 기준으로 특정 사용자의 이력 조회
}

