package com.example.bob.Repository;

import com.example.bob.Entity.ResumePortfolioEntity;
import com.example.bob.Entity.ResumeSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumePortfolioRepository extends JpaRepository<ResumePortfolioEntity, Long> {
    List<ResumePortfolioEntity> findByResumeSection(ResumeSectionEntity section);
}
