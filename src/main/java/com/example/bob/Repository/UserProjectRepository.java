package com.example.bob.Repository;

import com.example.bob.Entity.UserProjectEntity;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.bob.Entity.ProjectEntity;



import java.util.List;

public interface UserProjectRepository extends JpaRepository<UserProjectEntity, Long> {

    // ✅ 사용자가 승인된 프로젝트만 조회하는 기존 메서드
    List<UserProjectEntity> findByUserAndStatus(UserEntity user, String status);

    // ✅ 사용자가 특정 프로젝트에 이미 신청했는지 확인하는 메서드 추가
    @Query("SELECT COUNT(up) > 0 FROM UserProjectEntity up WHERE up.user = :user AND up.project = :project")
    boolean existsByUserAndProject(@Param("user") UserEntity user, @Param("project") ProjectEntity project);



    // 특정 프로젝트에 참가한 모든 사용자 찾기
    List<UserProjectEntity> findByProjectId(Long projectId);
}
