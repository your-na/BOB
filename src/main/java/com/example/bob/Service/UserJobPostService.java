package com.example.bob.Service;

import com.example.bob.DTO.UserJobPostDetailDTO;
import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Repository.CoJobPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserJobPostService {

    @Autowired
    private CoJobPostRepository coJobPostRepository;

    // 공고 상세 정보 + 이력서 제목 리스트 반환
    public UserJobPostDetailDTO getJobPostDetail(Long jobId) {
        // ID로 공고 조회 (없으면 예외 발생)
        CoJobPostEntity jobPost = coJobPostRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("해당 공고를 찾을 수 없습니다."));

        // 이력서 제목만 추출
        List<String> resumeTitles = jobPost.getResumes().stream()
                .map(CoResumeEntity::getTitle)
                .collect(Collectors.toList());

        // DTO로 변환하여 반환
        return new UserJobPostDetailDTO(
                jobPost.getTitle(),
                jobPost.getCompanyIntro(),
                jobPost.getEmail(),
                jobPost.getPhone(),
                jobPost.getCareer(),
                jobPost.getEducation(),
                jobPost.getEmploymentTypes(),
                jobPost.getSalary(),
                jobPost.getTime(),
                jobPost.getPreference(),
                jobPost.getStartDate(),
                jobPost.getEndDate(),
                resumeTitles // 이력서 양식 제목 추가
        );
    }
}

