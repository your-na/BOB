package com.example.bob.Service;

import com.example.bob.DTO.MyResumeDto;
import com.example.bob.DTO.MyResumeSectionDto;
import com.example.bob.Entity.MyResume;
import com.example.bob.Entity.MyResumeSection;
import com.example.bob.Repository.MyResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 나만의 이력서 저장 처리 서비스
 */
@Service
@RequiredArgsConstructor
public class MyResumeService {

    private final MyResumeRepository myResumeRepository;

    /**
     * 이력서 저장 처리 (프론트에서 전달한 DTO 기준)
     */
    public Long save(MyResumeDto dto) {
        // 이력서 엔티티 생성
        MyResume resume = MyResume.builder()
                .title(dto.getTitle())
                .memberId(dto.getMemberId())
                .build();

        // 각 섹션 DTO를 엔티티로 변환 후 이력서에 추가
        List<MyResumeSection> sections = dto.getSections().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        sections.forEach(resume::addSection); // 연관관계 설정 포함

        // DB 저장
        MyResume saved = myResumeRepository.save(resume);
        return saved.getId(); // 저장된 ID 반환
    }


    /**
     * 섹션 DTO → Entity 변환 메서드
     */
    private MyResumeSection convertToEntity(MyResumeSectionDto dto) {
        return MyResumeSection.builder()
                .type(dto.getType())
                .title(dto.getTitle())
                .comment(dto.getComment())
                .content(dto.getContent())
                .multiSelect(dto.isMultiSelect())
                .tags(dto.getTags())
                .conditions(dto.getConditions())
                .build();
    }

    /**
     * 특정 사용자의 모든 이력서 목록 조회
     */
    public List<MyResume> findAllByMemberId(Long memberId) {
        return myResumeRepository.findAllByMemberId(memberId);
    }

}
