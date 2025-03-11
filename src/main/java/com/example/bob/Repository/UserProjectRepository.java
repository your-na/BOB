package com.example.bob.Repository;

import com.example.bob.Entity.UserProjectEntity;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
<<<<<<< HEAD
=======
import java.util.Optional;
>>>>>>> origin/main

public interface UserProjectRepository extends JpaRepository<UserProjectEntity, Long> {

    // 사용자가 참가한 프로젝트 목록을 찾는 메서드
    List<UserProjectEntity> findByUser(UserEntity user);

    // 특정 프로젝트에 참가한 모든 사용자 찾기
    List<UserProjectEntity> findByProjectId(Long projectId);
<<<<<<< HEAD
=======

    // 프로젝트 ID와 사용자 ID로 신청 여부 확인 (특정 프로젝트에 참가한 특정 사용자)
    Optional<UserProjectEntity> findByProjectIdAndUserId(Long projectId, Long userId);
>>>>>>> origin/main
}
