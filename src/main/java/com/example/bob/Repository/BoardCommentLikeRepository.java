package com.example.bob.Repository;

import com.example.bob.Entity.BoardComment;
import com.example.bob.Entity.BoardCommentLike;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardCommentLikeRepository extends JpaRepository<BoardCommentLike, Long> {

    boolean existsByUserAndComment(UserEntity user, BoardComment comment);

    Optional<BoardCommentLike> findByUserAndComment(UserEntity user, BoardComment comment);

    int countByComment(BoardComment comment);
}
