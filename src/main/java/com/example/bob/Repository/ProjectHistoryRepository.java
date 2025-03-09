package com.example.bob.Repository;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.ProjectHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProjectHistoryRepository extends JpaRepository<ProjectHistoryEntity, Long> {

    // ✅ 특정 프로젝트의 히스토리 목록 조회 (시간순 정렬)
    List<ProjectHistoryEntity> findByProjectIdOrderByModifiedAtDesc(Long projectId);

    // ✅ 프로젝트와 연결된 모든 히스토리 삭제 (JPA Query Method 방식)
    @Transactional
    @Modifying
    void deleteByProject(ProjectEntity project);

    // ✅ 프로젝트 ID 기반 히스토리 삭제 (JPQL 직접 사용)
    @Transactional
    @Modifying
    @Query("DELETE FROM ProjectHistoryEntity h WHERE h.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
