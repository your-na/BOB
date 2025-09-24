package com.example.bob.Repository;

import com.example.bob.Entity.ContestAwardHistory;  // 수상 경력 엔티티
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestAwardRepository extends JpaRepository<ContestAwardHistory, Long> {
    List<ContestAwardHistory> findByUser(UserEntity user);
    List<ContestAwardHistory> findByUser_UserId(Long userId);
}
