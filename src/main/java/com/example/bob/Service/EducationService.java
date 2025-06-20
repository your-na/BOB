package com.example.bob.Service;

import com.example.bob.DTO.EducationSimpleDTO;
import com.example.bob.Entity.Education;
import com.example.bob.Repository.EducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;

    // ✅ 저장
    public Long save(Long userId, EducationSimpleDTO dto) {
        // ✅ 로그 출력으로 값 확인
        System.out.println("📌 [EducationService] 저장 시도");
        System.out.println("👉 userId: " + userId);
        System.out.println("👉 학교명: " + dto.getSchoolName());
        System.out.println("👉 학과명: " + dto.getMajorName());
        System.out.println("👉 상태: " + dto.getStatus());
        System.out.println("👉 시작일: " + dto.getStartDate());
        System.out.println("👉 종료일: " + dto.getEndDate());

        Education education = new Education();
        education.setUserId(userId);
        education.setSchoolName(dto.getSchoolName());
        education.setMajorName(dto.getMajorName());
        education.setStatus(dto.getStatus());
        education.setStartDate(dto.getStartDate());
        education.setEndDate(dto.getEndDate());

        return educationRepository.save(education).getId();
    }


    // ✅ 조회
    public List<EducationSimpleDTO> findByUserId(Long userId) {
        return educationRepository.findAllByUserId(userId).stream()
                .map(e -> new EducationSimpleDTO(
                        e.getSchoolName(),
                        e.getMajorName(),
                        e.getStatus(),
                        e.getStartDate(),
                        e.getEndDate()))
                .collect(Collectors.toList());
    }

    // ✅ 삭제
    public void deleteById(Long id) {
        educationRepository.deleteById(id);
    }
}
