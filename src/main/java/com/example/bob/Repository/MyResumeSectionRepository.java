package com.example.bob.Repository;

import com.example.bob.Entity.MyResumeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyResumeSectionRepository extends JpaRepository<MyResumeSection, Long> {
}
