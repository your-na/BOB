package com.example.bob.Repository;

import com.example.bob.Entity.ResumeFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 파일 및 사진 첨부 데이터를 저장/조회하기 위한 리포지토리.
 * ResumeFileEntity를 관리합니다.
 */
@Repository
public interface ResumeFileRepository extends JpaRepository<ResumeFileEntity, Long> {
}
