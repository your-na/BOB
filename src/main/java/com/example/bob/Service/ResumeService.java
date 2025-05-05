package com.example.bob.Service;

import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CoResumeSectionEntity;
import com.example.bob.Entity.CoResumeTagEntity;
import com.example.bob.Repository.CoResumeRepository;
import com.example.bob.DTO.ResumeDTO;
import com.example.bob.DTO.ResumeSectionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    @Autowired
    private CoResumeRepository coResumeRepository;

    // 기업 양식을 기반으로 사용자용 이력서 초기 구조를 생성
    public ResumeDTO generateUserResumeFromCo(Long coResumeId) {
        CoResumeEntity coResume = coResumeRepository.findById(coResumeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이력서 양식을 찾을 수 없습니다."));

        ResumeDTO resumeDTO = new ResumeDTO();
        resumeDTO.setTitle(coResume.getTitle());

        // 각 섹션 변환
        List<ResumeSectionDTO> sectionDTOs = coResume.getSections().stream()
                .map(section -> {
                    ResumeSectionDTO dto = new ResumeSectionDTO();
                    dto.setTitle(section.getTitle());
                    dto.setComment(section.getComment());
                    dto.setType(section.getType());
                    dto.setConditions(section.getConditions());
                    dto.setMultiSelect(section.isMultiSelect());
                    dto.setTags(
                            section.getSectionTags().stream()
                                    .map(CoResumeTagEntity::getTag)
                                    .collect(Collectors.toList())
                    );
                    return dto;
                })
                .collect(Collectors.toList());

        resumeDTO.setSections(sectionDTOs);

        // 희망직무 태그
        resumeDTO.setJobTags(
                coResume.getJobTags().stream()
                        .map(CoResumeTagEntity::getTag)
                        .collect(Collectors.toList())
        );

        return resumeDTO;
    }
}
