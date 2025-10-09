package com.example.bob.Repository;

import com.example.bob.Entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 프로젝트별 댓글 목록 (작성일자 오름차순)
    List<CommentEntity> findByProject_IdOrderByCreatedAtAsc(Long projectId);
}
