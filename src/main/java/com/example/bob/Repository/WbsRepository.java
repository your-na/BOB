package com.example.bob.Repository;

import com.example.bob.Entity.WbsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WbsRepository extends JpaRepository<WbsEntity, Long> {

    // 특정 대상(WBS)만 조회 (projectId 또는 contestId)
    List<WbsEntity> findByTypeAndTargetId(String type, Long targetId); //

    // 특정 작업 전체 삭제 (필요 시)
    void deleteByTypeAndTargetId(String type, Long targetId);

    List<WbsEntity> findByTypeAndTargetIdOrderByIdAsc(String type, Long targetId);

}
