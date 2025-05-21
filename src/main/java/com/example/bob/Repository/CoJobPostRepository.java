package com.example.bob.Repository;

import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Entity.CoResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;



public interface CoJobPostRepository extends JpaRepository<CoJobPostEntity, Long> {

    // 기업 ID로 공고 조회
    List<CoJobPostEntity> findByCompany_CompanyId(Long companyId);

    // 이력서 ID로 연결된 공고 조회 (JPQL 방식)
    @Query("SELECT j FROM CoJobPostEntity j JOIN j.resumes r WHERE r.id = :coResumeId")
    List<CoJobPostEntity> findByCoResumeId(@Param("coResumeId") Long coResumeId);

    // ✅ 특정 이력서(CoResume)가 포함된 공고 조회
    List<CoJobPostEntity> findAllByResumesContaining(CoResumeEntity coResume);

    // 기업이 작성한 공고 수
    int countByCompany_CompanyId(Long companyId);

    // 기업이 작성한 공고들의 ID 목록 기반으로 지원자 수 계산
    @Query("SELECT COUNT(a) FROM JobApplicationEntity a WHERE a.jobPost.company.companyId = :companyId")
    int countApplicantsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT j FROM CoJobPostEntity j WHERE j.company.companyId = :companyId ORDER BY j.id DESC")
    List<CoJobPostEntity> findTop3RecentPostsByCompany(@Param("companyId") Long companyId, Pageable pageable);


}
