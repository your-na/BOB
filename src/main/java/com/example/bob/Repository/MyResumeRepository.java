package com.example.bob.Repository;

import com.example.bob.Entity.MyResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 나만의 이력서 저장소
 */
@Repository
public interface MyResumeRepository extends JpaRepository<MyResume, Long> {

    // ✅ memberId 기준으로 이력서 목록 가져오기
    List<MyResume> findAllByMemberId(Long memberId);

    // ✅ 이력서 ID로 조회하면서 섹션도 함께 fetch (상세보기용)
    @Query("SELECT r FROM MyResume r LEFT JOIN FETCH r.sections WHERE r.id = :resumeId")
    Optional<MyResume> findByIdWithSections(@Param("resumeId") Long resumeId);
}
