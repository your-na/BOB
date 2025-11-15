package com.example.bob.Repository;

import com.example.bob.Entity.BoardPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPostLikeRepository extends JpaRepository<BoardPostLike, Long> {

    // 특정 게시글에 특정 유저가 좋아요 눌렀는지 확인
    boolean existsByBoardPostIdAndUsername(Long postId, String username);

    // 특정 게시글의 특정 유저 좋아요 삭제(좋아요 취소)
    void deleteByBoardPostIdAndUsername(Long postId, String username);

    // 특정 게시글 좋아요 개수
    long countByBoardPostId(Long postId);
}
