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

    // ✅ 저장 (id 존재 시 update, 없으면 insert)
    public Long save(Long userId, EducationSimpleDTO dto) {
        System.out.println("📌 [EducationService] 저장 시도");
        System.out.println("👉 id: " + dto.getId());
        System.out.println("👉 userId: " + userId);
        System.out.println("👉 id: " + dto.getId());
        System.out.println("👉 학교명: " + dto.getSchoolName());
        System.out.println("👉 학과명: " + dto.getMajorName());
        System.out.println("👉 상태: " + dto.getStatus());
        System.out.println("👉 시작일: " + dto.getStartDate());
        System.out.println("👉 종료일: " + dto.getEndDate());

        Education education;

        // ✅ 기존 항목 수정
        if (dto.getId() != null) {
            education = educationRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("해당 학력 정보를 찾을 수 없습니다."));

            education.setSchoolName(dto.getSchoolName());
            education.setMajorName(dto.getMajorName());
            education.setStatus(dto.getStatus());
            education.setStartDate(dto.getStartDate());
            education.setEndDate(dto.getEndDate());

            System.out.println("✏ 기존 학력 수정 완료 (id=" + dto.getId() + ")");
        }
        // ✅ 새 항목 추가
        else {
            education = new Education();
            education.setUserId(userId);
            education.setSchoolName(dto.getSchoolName());
            education.setMajorName(dto.getMajorName());
            education.setStatus(dto.getStatus());
            education.setStartDate(dto.getStartDate());
            education.setEndDate(dto.getEndDate());

            System.out.println("🆕 새 학력 추가");
        }

        return educationRepository.save(education).getId();
    }

    // ✅ 조회
    public List<EducationSimpleDTO> findByUserId(Long userId) {
        return educationRepository.findAllByUserId(userId).stream()
                .map(e -> new EducationSimpleDTO(
                        e.getId(), // ← id도 함께 DTO에 담기
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
