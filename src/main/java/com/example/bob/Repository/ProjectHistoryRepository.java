package com.example.bob.Repository;

import com.example.bob.Entity.ProjectHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectHistoryRepository extends JpaRepository<ProjectHistoryEntity, Long> {
    // ✅ 특정 프로젝트의 수정 및 삭제 이력을 시간순으로 조회하는 메서드
    List<ProjectHistoryEntity> findByProjectIdOrderByModifiedAtDesc(Long projectId);
}
