package com.example.bob.Repository;

import com.example.bob.Entity.ContestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRepository extends JpaRepository<ContestEntity, Long> {
    List<ContestEntity> findByIsApprovedTrue(); // 승인된 공모전만 조회
    List<ContestEntity> findByIsApprovedFalse(); // 미승인 공모전 조회 (관리자용)

    List<ContestEntity> findAllByOrderByCreatedAtDesc();
}
