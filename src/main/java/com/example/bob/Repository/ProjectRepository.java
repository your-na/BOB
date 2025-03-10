package com.example.bob.Repository;

import com.example.bob.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    // ✅ "INACTIVE" 상태가 아닌 프로젝트만 조회 (삭제된 프로젝트 제외)
    @Query("SELECT p FROM ProjectEntity p WHERE p.status <> 'INACTIVE'")
    List<ProjectEntity> findAllActiveProjects();
}
