package com.example.bob.Repository;

import com.example.bob.Entity.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {


    // 사용자 ID로 이력서 조회
    List<ResumeEntity> findByUser_UserId(Long userId);

    // 🔥 공고 삭제 시 제출 이력서(resume_entity) 먼저 삭제하는 데 필요!
    void deleteAllByJobPost_Id(Long jobPostId);

}


