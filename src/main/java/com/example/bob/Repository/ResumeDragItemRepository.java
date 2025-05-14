package com.example.bob.Repository;

import com.example.bob.Entity.ResumeDragItemEntity;
import com.example.bob.Entity.ResumeSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeDragItemRepository extends JpaRepository<ResumeDragItemEntity, Long> {

    // 📌 특정 섹션에 드래그로 추가된 항목들 조회
    List<ResumeDragItemEntity> findBySection(ResumeSectionEntity section);
}
