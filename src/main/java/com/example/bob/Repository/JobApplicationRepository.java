package com.example.bob.Repository;

import com.example.bob.Entity.JobApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, Long> {

    // ✅ 사용자 ID로 지원 내역 조회
    List<JobApplicationEntity> findByUser_UserId(Long userId);
}
