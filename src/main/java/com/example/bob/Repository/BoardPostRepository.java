package com.example.bob.Repository;

import com.example.bob.Entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
    // 추가적으로 category별 조회 등도 여기서 구현 가능
}
