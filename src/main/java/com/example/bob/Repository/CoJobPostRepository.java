package com.example.bob.Repository;

import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Entity.CoResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CoJobPostRepository extends JpaRepository<CoJobPostEntity, Long> {

    // 기업 ID로 공고 조회
    List<CoJobPostEntity> findByCompany_CompanyId(Long companyId);

    // 이력서 ID로 연결된 공고 조회 (JPQL 방식)
    @Query("SELECT j FROM CoJobPostEntity j JOIN j.resumes r WHERE r.id = :coResumeId")
    List<CoJobPostEntity> findByCoResumeId(@Param("coResumeId") Long coResumeId);

    // ✅ 특정 이력서(CoResume)가 포함된 공고 조회
    List<CoJobPostEntity> findAllByResumesContaining(CoResumeEntity coResume);
}
