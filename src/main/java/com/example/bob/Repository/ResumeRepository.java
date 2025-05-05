package com.example.bob.Repository;

import com.example.bob.Entity.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
    // 필요시 사용자 ID 또는 coResumeId로 조회하는 커스텀 쿼리 추가 가능
}
