package com.example.bob.Repository;

import com.example.bob.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    // ✅ "INACTIVE" 상태가 아닌 프로젝트만 조회 (삭제된 프로젝트 제외)
    @Query("SELECT p FROM ProjectEntity p WHERE p.status <> 'INACTIVE'")
    List<ProjectEntity> findAllActiveProjects();

    // ✅ 기존 ACTIVE 데이터를 모집중으로 변경 (시작 날짜가 미래인 경우)
    @Transactional
    @Modifying
    @Query("UPDATE ProjectEntity p SET p.status = '모집중' WHERE p.status = 'ACTIVE' AND p.startDate > :today")
    void updateOldActiveToRecruiting(LocalDate today);

    // ✅ 기존 ACTIVE 데이터를 진행중으로 변경 (시작 날짜가 오늘 또는 지난 경우)
    @Transactional
    @Modifying
    @Query("UPDATE ProjectEntity p SET p.status = '진행중' WHERE p.status = 'ACTIVE' AND p.startDate <= :today")
    void updateOldActiveToOngoing(LocalDate today);

    // ✅ 사용자 닉네임으로 프로젝트를 찾는 메서드 추가
    List<ProjectEntity> findByCreatedBy(String createdBy);
}
