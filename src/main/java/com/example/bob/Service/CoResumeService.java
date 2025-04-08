package com.example.bob.Service; //기능 "선언"만 하는 인터페이스

import com.example.bob.DTO.CoResumeRequestDTO;

public interface CoResumeService {
    void saveResume(CoResumeRequestDTO requestDTO); // 이력서 저장 기능
}
