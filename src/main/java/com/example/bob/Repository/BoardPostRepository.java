package com.example.bob.Repository;

import com.example.bob.Entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    List<BoardPost> findAllByOrderByCreatedAtDesc(); //게시판 목록용

    List<BoardPost> findByWriterOrderByCreatedAtDesc(String writer);  // ✅ 내가 쓴 게시글 목록 조회

    List<BoardPost> findAllByIdInAndWriter(List<Long> ids, String writer);   // 게시글 삭제



}
