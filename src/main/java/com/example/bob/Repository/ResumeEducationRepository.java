package com.example.bob.Repository;

import com.example.bob.Entity.ResumeEducationEntity;
import com.example.bob.Entity.ResumeSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducationEntity, Long> {

    // 📌 특정 섹션에 연결된 학력 항목들 조회
    List<ResumeEducationEntity> findByResumeSection(ResumeSectionEntity section);
}
