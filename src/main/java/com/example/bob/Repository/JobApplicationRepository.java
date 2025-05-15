package com.example.bob.Repository;

import com.example.bob.Entity.JobApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bob.Entity.UserEntity;


import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, Long> {

    // ✅ 사용자 ID로 지원 내역 조회
    List<JobApplicationEntity> findByUser_UserId(Long userId);

    // ✅ 공고 ID + 사용자 기준으로 이력서 조회
    Optional<JobApplicationEntity> findByUserAndJobPost_Id(UserEntity user, Long jobPostId);

    // 마지막으로 제출한 지원 내역 1건만 가져오기 (appliedAt 기준 내림차순 정렬)
    Optional<JobApplicationEntity> findTopByUserAndJobPost_IdOrderByAppliedAtDesc(UserEntity user, Long jobPostId);

    // ✅ 중복 지원 여부 확인용 메서드 추가
    boolean existsByUserAndJobPost_Id(UserEntity user, Long jobPostId);


}
