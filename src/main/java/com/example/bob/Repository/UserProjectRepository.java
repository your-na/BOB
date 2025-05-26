package com.example.bob.Repository;

import com.example.bob.Entity.UserProjectEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;
import java.util.List;

public interface UserProjectRepository extends JpaRepository<UserProjectEntity, Long> {

    // ✅ 사용자가 신청중인 프로젝트만 조회하는 메서드
    List<UserProjectEntity> findByUserAndStatus(UserEntity user, String status);

    // ✅ 사용자가 특정 프로젝트에 이미 신청했는지 확인
    @Query("SELECT COUNT(up) > 0 FROM UserProjectEntity up WHERE up.user = :user AND up.project = :project")
    boolean existsByUserAndProject(@Param("user") UserEntity user, @Param("project") ProjectEntity project);

    // ✅ 특정 사용자와 특정 프로젝트의 신청 정보 가져오기
    Optional<UserProjectEntity> findByUserAndProject(UserEntity user, ProjectEntity project);

    // ✅ 특정 사용자와 프로젝트에 대한 정보 가져오기 (🔹 userId와 projectId를 올바르게 참조)
    Optional<UserProjectEntity> findByUser_UserIdAndProject_Id(Long userId, Long projectId);

    // ✅ 파일을 제출한 프로젝트만 조회 (🔹 특정 사용자가 파일을 제출한 프로젝트만 가져오기)
    List<UserProjectEntity> findByUser_UserIdAndSubmittedFileNameIsNotNull(Long userId);

    // ✅ 특정 사용자가 제출한 모든 프로젝트 목록 조회 (중복 제거)
    List<UserProjectEntity> findByUser(UserEntity user);

    List<UserProjectEntity> findByUserAndStatusIn(UserEntity user, List<String> statuses);

    List<UserProjectEntity> findByUser_UserIdAndStatusAndSubmittedFileNameIsNotNullAndVisibleTrue(Long userId, String status);

    // ✅ 특정 프로젝트 + 상태 조건으로 유저 프로젝트 조회
    List<UserProjectEntity> findByProjectAndStatusIn(ProjectEntity project, List<String> statusList);

    // ✅ 특정 프로젝트 ID와 사용자 ID로 제출 정보 가져오기
    Optional<UserProjectEntity> findByProject_IdAndUser_UserId(Long projectId, Long userId);










    // ✅ 프로젝트와 관련된 팀 신청 삭제
    @Modifying
    @Query("DELETE FROM UserProjectEntity up WHERE up.project = :project")
    void deleteByProject(@Param("project") ProjectEntity project);

    // ✅ 특정 프로젝트에 속한 모든 UserProjectEntity 조회
    List<UserProjectEntity> findByProject(ProjectEntity project);

    // 유저의 userId와 "완료" 상태에 해당하는 UserProjectEntity 목록을 조회
    List<UserProjectEntity> findByUser_UserIdAndStatus(Long userId, String status);
}

