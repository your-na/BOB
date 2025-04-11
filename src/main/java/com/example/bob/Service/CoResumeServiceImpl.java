package com.example.bob.Service;

import com.example.bob.Controller.CoResumeEditController;
import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.DTO.CoResumeSectionRequestDTO;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CoResumeSectionEntity;
import com.example.bob.Repository.CoResumeRepository;
import com.example.bob.Repository.CoResumeSectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public void saveResume(CoResumeRequestDTO requestDTO) {
        logger.info("이력서 저장 요청 - 제목: {}", requestDTO.getTitle());

        CoResumeEntity resume = new CoResumeEntity();
        resume.setTitle(requestDTO.getTitle());

        // 희망직무 태그 저장
        List<String> jobTags = requestDTO.getJobTags();
        if (jobTags != null && !jobTags.isEmpty()) {
            resume.setJobTags(jobTags);  // 희망직무 태그를 이력서에 추가
        }

        // 섹션들 추가
        if (requestDTO.getSections() != null) {
            for (CoResumeSectionRequestDTO sectionDTO : requestDTO.getSections()) {
                // 로그 추가: multiSelect 값 확인
                logger.debug("복수선택 여부: {}", sectionDTO.isMultiSelect());

                CoResumeSectionEntity sectionEntity = new CoResumeSectionEntity();
                sectionEntity.setType(sectionDTO.getType());
                sectionEntity.setTitle(sectionDTO.getTitle());
                sectionEntity.setComment(sectionDTO.getComment());
                sectionEntity.setContent(sectionDTO.getContent());
                sectionEntity.setTags(sectionDTO.getTags());  // 태그 저장

                // multiSelect 값은 sectionDTO에서 받아온 값에 따라 설정
                sectionEntity.setMultiSelect(sectionDTO.isMultiSelect());  // 복수선택 여부

                sectionEntity.setDirectInputValue(sectionDTO.getDirectInputValue()); // 직접입력 값

                // 조건 항목들을 저장하는 부분
                sectionEntity.setConditions(sectionDTO.getConditions()); // 조건 항목들 저장
                logger.info("조건 항목 저장됨: {}", sectionDTO.getConditions()); // 조건 항목 로그

                // 연관 관계 설정
                resume.addSection(sectionEntity);
            }
        }

        // 이력서 저장
        coResumeRepository.save(resume);
        logger.info("이력서 저장 완료 - 제목: {}", requestDTO.getTitle());  // 저장 완료 로그
    }


    // ✅ 이력서 조회 (수정용)
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
                            sectionEntity.isMultiSelect(),  // multiSelect 값 전달
                            sectionEntity.getConditions(),
                            sectionEntity.getDirectInputValue()
                    ))
                    .collect(Collectors.toList());

            // 반환할 이력서 정보에 희망직무 태그도 포함
            return new CoResumeRequestDTO(
                    resumeEntity.getTitle(),
                    sectionDTOList,
                    resumeEntity.getCreatedAt(),
                    resumeEntity.getJobTags()  // 희망직무 태그를 반환
            );
        } else {
            logger.error("이력서를 찾을 수 없습니다 - ID: {}", id);
            throw new RuntimeException("이력서를 찾을 수 없습니다.");
        }
    }

    // ✅ 이력서 업데이트
    @Override
    public void updateResume(Long id, CoResumeRequestDTO updatedResume) {
        CoResumeEntity resumeEntity = coResumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이력서가 존재하지 않습니다."));

        resumeEntity.setTitle(updatedResume.getTitle());
        resumeEntity.setCreatedAt(updatedResume.getCreatedAt());

        // 희망직무 태그 업데이트
        List<String> jobTags = updatedResume.getJobTags();
        if (jobTags != null && !jobTags.isEmpty()) {
            resumeEntity.setJobTags(jobTags);  // 희망직무 태그 업데이트
        }

        List<CoResumeSectionEntity> updatedSections = updatedResume.getSections().stream()
                .map(sectionDTO -> {
                    // 로그 추가: multiSelect 값 확인
                    logger.debug("복수선택 여부: {}", sectionDTO.isMultiSelect());

                    CoResumeSectionEntity sectionEntity = new CoResumeSectionEntity();
                    sectionEntity.setTitle(sectionDTO.getTitle());
                    sectionEntity.setType(sectionDTO.getType());
                    sectionEntity.setComment(sectionDTO.getComment());
                    sectionEntity.setContent(sectionDTO.getContent());
                    sectionEntity.setTags(sectionDTO.getTags());
                    sectionEntity.setMultiSelect(sectionDTO.isMultiSelect()); // 복수선택 여부
                    sectionEntity.setConditions(sectionDTO.getConditions()); // 조건 항목들
                    sectionEntity.setDirectInputValue(sectionDTO.getDirectInputValue()); // 직접입력 값
                    sectionEntity.setResume(resumeEntity); // 연관된 이력서 설정
                    return sectionEntity;
                })
                .collect(Collectors.toList());




        coResumeSectionRepository.deleteAll(resumeEntity.getSections());
        resumeEntity.setSections(updatedSections);

        coResumeRepository.save(resumeEntity);
    }

    // ✅ 목록 조회
    @Override
    public List<CoResumeEntity> getAllResumes() {
        return coResumeRepository.findAll();
    }

    // ✅ 삭제
    @Override
    public void deleteResume(Long id) {
        coResumeRepository.deleteById(id);
    }
}
