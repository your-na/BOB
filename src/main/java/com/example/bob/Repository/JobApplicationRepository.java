    package com.example.bob.Repository;

    import com.example.bob.Entity.JobApplicationEntity;
    import org.springframework.data.jpa.repository.JpaRepository;
    import com.example.bob.Entity.UserEntity;
    import com.example.bob.Entity.JobApplicationStatus;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import java.util.Date;




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

        // ✅ 중복 지원 여부 확인 (SUBMITTED 상태만)
        boolean existsByUserAndJobPost_IdAndStatus(UserEntity user, Long jobPostId, JobApplicationStatus status);

        // ✅ 가장 최근 SUBMITTED 상태 지원 내역 1건 조회
        Optional<JobApplicationEntity> findTopByUserAndJobPost_IdAndStatusOrderByAppliedAtDesc(
                UserEntity user,
                Long jobPostId,
                JobApplicationStatus status
        );

        // ✅ 공고 ID 기준으로 지원자 수 세기
        int countByJobPost_Id(Long jobPostId);

        // ✅ 공고별로 유저 기준 중복 없이 지원자 수 세기
        @Query("SELECT COUNT(DISTINCT a.user.userId) FROM JobApplicationEntity a WHERE a.jobPost.id = :jobPostId")
        int countDistinctApplicantsByJobPostId(@Param("jobPostId") Long jobPostId);


        // ✅ 공고 ID와 상태로 지원자 수 카운트 💼
        int countByJobPost_IdAndStatus(Long jobPostId, JobApplicationStatus status);

        // ✅ 공고 ID + 상태로 지원자 목록 가져오기 (예: SUBMITTED만)
        List<JobApplicationEntity> findByJobPost_IdAndStatus(Long jobPostId, JobApplicationStatus status);

        // ✅ 기업이 가장 최근 제출된 지원서 1건 조회
        Optional<JobApplicationEntity> findTopByJobPost_IdAndStatusOrderByAppliedAtDesc(Long jobPostId, JobApplicationStatus status);

        // ✅ 이력서 기준으로 가장 최근 지원 내역 조회 (resumeId → 공고정보 복구용)
        Optional<JobApplicationEntity> findTopByResumeOrderByAppliedAtDesc(com.example.bob.Entity.ResumeEntity resume);

        @Query("""
    SELECT a 
    FROM JobApplicationEntity a
    WHERE a.jobPost.company.companyId = :companyId
    AND a.status != 'HIDDEN'
    ORDER BY a.appliedAt DESC
""")
        List<JobApplicationEntity> findTop3RecentApplicants(@Param("companyId") Long companyId, Pageable pageable);

        // ✅ 특정 기업의 합격된 지원자 목록 조회
        @Query("SELECT j FROM JobApplicationEntity j WHERE j.jobPost.company.companyId = :companyId AND j.status = 'ACCEPTED'")
        List<JobApplicationEntity> findAcceptedApplicationsByCompany(@Param("companyId") Long companyId);

        // 최근 1년간 중복 제거한 지원자 수 조회
        @Query("SELECT COUNT(DISTINCT ja.user.userId) FROM JobApplicationEntity ja WHERE ja.appliedAt >= :startDate")
        long countDistinctApplicantsSince(@Param("startDate") Date startDate);

        // 최근 1년간 중복 제거한 합격자 수 조회
        @Query("SELECT COUNT(DISTINCT ja.user.userId) FROM JobApplicationEntity ja WHERE ja.status = 'ACCEPTED' AND ja.appliedAt >= :startDate")
        long countDistinctAcceptedSince(@Param("startDate") Date startDate);

        // ✅ 내가 만든 이력서 기준으로 가장 최근 지원 내역 조회
        Optional<JobApplicationEntity> findTopByMyResumeOrderByAppliedAtDesc(com.example.bob.Entity.MyResume myResume);

        // ✅ 중복 지원 여부 확인 (내 이력서 기준 + 상태)
        boolean existsByUserAndJobPost_IdAndMyResumeAndStatus(
                UserEntity user,
                Long jobPostId,
                com.example.bob.Entity.MyResume myResume,
                JobApplicationStatus status
        );











    }
