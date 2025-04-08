package com.example.bob.Service; //기능 "선언"만 하는 인터페이스

import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.Entity.CoResumeEntity;
import java.util.List;

public interface CoResumeService {
    void saveResume(CoResumeRequestDTO requestDTO); // 이력서 저장 기능

    // ✅ 목록 조회 메서드 추가
    List<CoResumeEntity> getAllResumes();
}
