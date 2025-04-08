package com.example.bob.Repository;

import com.example.bob.Entity.CoResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 기업 이력서 양식 저장/조회용 Repository
 */
@Repository
public interface CoResumeRepository extends JpaRepository<CoResumeEntity, Long> {
    // 기본적인 save(), findAll(), findById() 전부 제공됨
}
