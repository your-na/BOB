package com.example.bob.Repository;

import com.example.bob.Entity.BoardComment;
import com.example.bob.Entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    // 특정 게시글의 댓글 목록 조회 (최신순)
    List<BoardComment> findByBoardPostOrderByCreatedAtAsc(BoardPost post);
}
