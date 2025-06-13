package com.example.bob.Repository;

import com.example.bob.Entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {

    // 특정 사용자(userId)의 학력 목록 조회
    List<Education> findAllByUserId(Long userId);
}
