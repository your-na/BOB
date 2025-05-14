package com.example.bob.Repository;

import com.example.bob.Entity.ResumeFileEntity;
import com.example.bob.Entity.ResumeSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeFileRepository extends JpaRepository<ResumeFileEntity, Long> {

    // 📌 특정 섹션에 업로드된 파일 목록 조회
    List<ResumeFileEntity> findByResumeSection(ResumeSectionEntity section);
}
