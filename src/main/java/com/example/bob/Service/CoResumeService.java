package com.example.bob.Service; //기능 "선언"만 하는 인터페이스

import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.Entity.CoResumeEntity;

import java.util.List;

public interface CoResumeService {

    // ✅ 이력서 저장 기능
    void saveResume(CoResumeRequestDTO requestDTO);

    // ✅ 목록 조회 메서드 추가
    List<CoResumeEntity> getAllResumes();

    // ✅ 삭제 메서드 추가
    void deleteResume(Long id);

    // ✅ 이력서 수정용 조회 기능
    CoResumeRequestDTO getResumeById(Long id);

    // ✅ 이력서 수정 기능
    void updateResume(Long id, CoResumeRequestDTO updatedResume);

}
