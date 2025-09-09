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

    // ✅ memberId (String) 기준으로 이력서 목록 가져오기
    List<MyResume> findAllByMemberId(String memberId);  // ⚠️ 여기 타입을 String으로 바꿔야 에러 안 남

    // ✅ 이력서 ID로 조회하면서 섹션도 함께 fetch (상세보기용)
    @Query("""
    SELECT DISTINCT r
    FROM MyResume r
    LEFT JOIN FETCH r.sections s
    LEFT JOIN FETCH s.dragItems
    WHERE r.id = :resumeId
    """)
    Optional<MyResume> findByIdWithSections(@Param("resumeId") Long resumeId);

    // ✅ 이력서 삭제 시 본인 것만 삭제되도록
    void deleteByIdAndMemberId(Long id, String memberId);


}
