package com.example.bob.Repository;

import com.example.bob.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    // ✅ 사용자 닉네임으로 프로젝트를 찾는 메서드 추가
    List<ProjectEntity> findByCreatedBy(String createdBy);

    // ✅ "완료" 상태가 아닌 프로젝트만 조회
    @Query("SELECT p FROM ProjectEntity p WHERE p.status <> '완료'")
    List<ProjectEntity> findAllActiveProjects();

    Optional<ProjectEntity> findByTitle(String title);

    // "완료" 상태를 제외하고 최신 프로젝트 ID 기준으로 내림차순 정렬
    @Query("SELECT p FROM ProjectEntity p WHERE p.status != '완료' ORDER BY p.id DESC")
    List<ProjectEntity> findAllActiveProjectsSortedById();

    // ✅ "완료" 상태가 아닌 프로젝트를 페이징 처리하여 조회
    @Query("SELECT p FROM ProjectEntity p WHERE p.status != '완료' ORDER BY p.id DESC")
    Page<ProjectEntity> findAllActiveProjectsPaged(Pageable pageable);


}



