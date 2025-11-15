package com.example.bob.Repository;

import com.example.bob.Entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
    List<BoardPost> findAllByOrderByCreatedAtDesc(); //게시판 목록용
}
