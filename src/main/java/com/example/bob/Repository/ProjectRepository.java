package com.example.bob.Repository;

import com.example.bob.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    // ✅ 사용자 닉네임으로 프로젝트를 찾는 메서드 추가
    List<ProjectEntity> findByCreatedBy(String createdBy);

    // ✅ "완료" 상태가 아닌 프로젝트만 조회
    @Query("SELECT p FROM ProjectEntity p WHERE p.status <> '완료'")
    List<ProjectEntity> findAllActiveProjects();

}
