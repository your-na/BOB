package com.example.bob.Repository;

import com.example.bob.Entity.MyResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // 이거 꼭 import!

/**
 * 나만의 이력서 저장소
 */
@Repository
public interface MyResumeRepository extends JpaRepository<MyResume, Long> {

    // ✅ memberId 기준으로 이력서 목록 가져오기
    List<MyResume> findAllByMemberId(Long memberId);
}
