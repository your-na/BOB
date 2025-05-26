package com.example.bob.Repository;

import com.example.bob.Entity.ContestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRepository extends JpaRepository<ContestEntity, Long> {
    List<ContestEntity> findByIsApprovedTrue(); // 승인된 공모전만 조회
    List<ContestEntity> findByIsApprovedFalse(); // 미승인 공모전 조회 (관리자용)

    List<ContestEntity> findByIsApprovedFalseAndIsDeletedFalse();
    List<ContestEntity> findAllByOrderByCreatedAtDesc();

    List<ContestEntity> findByIsApprovedTrueAndIsDeletedFalse(); // 사용자용 목록
    List<ContestEntity> findByIsDeletedFalse(); // 관리자용 전체 목록

    List<ContestEntity> findByCreatorTypeAndIsDeletedFalse(String creatorType);


    List<ContestEntity> findTop4ByIsApprovedTrueOrderByCreatedAtDesc(); //메인쪽에 4개 뜨게 하려는거임

}
