package com.example.bob.Repository;

import com.example.bob.Entity.ResumeCareerEntity;
import com.example.bob.Entity.ResumeSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeCareerRepository extends JpaRepository<ResumeCareerEntity, Long> {
    List<ResumeCareerEntity> findByResumeSection(ResumeSectionEntity section);
}
