package com.example.bob.Service;

import com.example.bob.Controller.CoResumeEditController;
import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.DTO.CoResumeSectionRequestDTO;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CoResumeSectionEntity;
import com.example.bob.Entity.CoResumeTagEntity;
import com.example.bob.Repository.CoResumeRepository;
import com.example.bob.Repository.CoResumeSectionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CoResumeServiceImpl implements CoResumeService {

    private final CoResumeRepository coResumeRepository;
    private final CoResumeSectionRepository coResumeSectionRepository;
    private static final Logger logger = LoggerFactory.getLogger(CoResumeEditController.class);

    @Autowired
    public CoResumeServiceImpl(CoResumeRepository coResumeRepository, CoResumeSectionRepository coResumeSectionRepository) {
        this.coResumeRepository = coResumeRepository;
        this.coResumeSectionRepository = coResumeSectionRepository;
    }

    // ✅ 이력서 저장 (제작)
    @Override
    public void saveResume(CoResumeRequestDTO requestDTO) {
        logger.info("이력서 저장 요청 - 제목: {}", requestDTO.getTitle());

        CoResumeEntity resume = new CoResumeEntity();
        resume.setTitle(requestDTO.getTitle());

        List<CoResumeTagEntity> tagEntities = new ArrayList<>();

        if (requestDTO.getSections() != null) {
            for (CoResumeSectionRequestDTO sectionDTO : requestDTO.getSections()) {
                CoResumeSectionEntity sectionEntity = new CoResumeSectionEntity();
                sectionEntity.setType(sectionDTO.getType());
                sectionEntity.setTitle(sectionDTO.getTitle());
                sectionEntity.setComment(sectionDTO.getComment());
                sectionEntity.setContent(sectionDTO.getContent());
                sectionEntity.setMultiSelect(sectionDTO.isMultiSelect());
                sectionEntity.setDirectInputValue(sectionDTO.getDirectInputValue());
                sectionEntity.setConditions(sectionDTO.getConditions());

                // ✅ 태그 처리
                if (sectionDTO.getTags() != null) {
                    for (String tagValue : sectionDTO.getTags()) {
                        CoResumeTagEntity tag = new CoResumeTagEntity();
                        tag.setTag(tagValue);
                        tag.setResume(resume);                // 이력서 연관
                        tag.setSection(sectionEntity);        // ✅ 섹션 연관 (이게 핵심!)
                        tagEntities.add(tag);
                    }
                    sectionEntity.setTags(sectionDTO.getTags());
                } else {
                    sectionEntity.setTags(new ArrayList<>());
                }

                resume.addSection(sectionEntity);
            }
        }

        // ✅ 희망직무 태그 처리
        if (requestDTO.getJobTags() != null) {
            for (String tagValue : requestDTO.getJobTags()) {
                CoResumeTagEntity tag = new CoResumeTagEntity();
                tag.setTag(tagValue);
                tag.setResume(resume); // 희망직무는 resume만 설정
                tagEntities.add(tag);
            }
        }

        resume.setJobTags(tagEntities);
        coResumeRepository.save(resume);

        logger.info("이력서 저장 완료 - 제목: {}", requestDTO.getTitle());
    }


    // ✅ 이력서 조회
    @Override
    public CoResumeRequestDTO getResumeById(Long id) {
        logger.info("이력서 데이터 불러오기 요청 - ID: {}", id);

        Optional<CoResumeEntity> resumeEntityOpt = coResumeRepository.findById(id);
        if (resumeEntityOpt.isPresent()) {
            CoResumeEntity resumeEntity = resumeEntityOpt.get();
            logger.info("이력서 데이터 불러오기 성공 - ID: {}", id);

            List<CoResumeSectionRequestDTO> sectionDTOList = resumeEntity.getSections().stream()
                    .map(sectionEntity -> new CoResumeSectionRequestDTO(
                            sectionEntity.getType(),
                            sectionEntity.getTitle(),
                            sectionEntity.getComment(),
                            sectionEntity.getContent(),
                            sectionEntity.getTags(),
                            sectionEntity.isMultiSelect(),
                            sectionEntity.getConditions(),
                            sectionEntity.getDirectInputValue()
                    ))
                    .collect(Collectors.toList());

            // jobTags에서 문자열만 추출
            List<String> jobTagStrings = resumeEntity.getJobTags().stream()
                    .map(CoResumeTagEntity::getTag)
                    .collect(Collectors.toList());

            return new CoResumeRequestDTO(
                    resumeEntity.getTitle(),
                    sectionDTOList,
                    resumeEntity.getCreatedAt(),
                    jobTagStrings
            );
        } else {
            logger.error("이력서를 찾을 수 없습니다 - ID: {}", id);
            throw new RuntimeException("이력서를 찾을 수 없습니다.");
        }
    }

    // ✅ 이력서 수정
    @Override
    public void updateResume(Long id, CoResumeRequestDTO updatedResume) {
        CoResumeEntity resumeEntity = coResumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이력서가 존재하지 않습니다."));

        resumeEntity.setTitle(updatedResume.getTitle());
        resumeEntity.setCreatedAt(updatedResume.getCreatedAt());

        List<CoResumeSectionEntity> updatedSections = updatedResume.getSections().stream()
                .map(sectionDTO -> {
                    CoResumeSectionEntity sectionEntity = new CoResumeSectionEntity();
                    sectionEntity.setTitle(sectionDTO.getTitle());
                    sectionEntity.setType(sectionDTO.getType());
                    sectionEntity.setComment(sectionDTO.getComment());
                    sectionEntity.setContent(sectionDTO.getContent());
                    sectionEntity.setTags(sectionDTO.getTags());
                    sectionEntity.setMultiSelect(sectionDTO.isMultiSelect());
                    sectionEntity.setConditions(sectionDTO.getConditions());
                    sectionEntity.setDirectInputValue(sectionDTO.getDirectInputValue());
                    sectionEntity.setResume(resumeEntity);
                    return sectionEntity;
                })
                .collect(Collectors.toList());

        coResumeSectionRepository.deleteAll(resumeEntity.getSections());
        resumeEntity.setSections(updatedSections);

        // 태그도 모두 새로 갱신
        List<CoResumeTagEntity> updatedTags = new ArrayList<>();
        if (updatedResume.getJobTags() != null) {
            for (String tagValue : updatedResume.getJobTags()) {
                CoResumeTagEntity tag = new CoResumeTagEntity();
                tag.setTag(tagValue);
                tag.setResume(resumeEntity);
                updatedTags.add(tag);
            }
        }
        resumeEntity.setJobTags(updatedTags);

        coResumeRepository.save(resumeEntity);
    }

    // ✅ 이력서 전체 목록 조회
    @Override
    public List<CoResumeEntity> getAllResumes() {
        return coResumeRepository.findAll();
    }

    // ✅ 이력서 삭제
    @Override
    public void deleteResume(Long id) {
        coResumeRepository.deleteById(id);
    }
}
