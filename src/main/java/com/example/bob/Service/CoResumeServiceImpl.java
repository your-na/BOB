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

            // 1️⃣ 전체 태그 리스트 (희망직무 + 선택형 섹션 포함)
            List<CoResumeTagEntity> allTags = resumeEntity.getJobTags();

            // 2️⃣ 섹션 DTO 구성
            List<CoResumeSectionRequestDTO> sectionDTOList = resumeEntity.getSections().stream()
                    .map(sectionEntity -> {
                        // 2-1️⃣ 해당 섹션과 연결된 태그만 필터링
                        List<String> tagsForThisSection = allTags.stream()
                                .filter(tag -> tag.getSection() != null && tag.getSection().equals(sectionEntity))
                                .map(CoResumeTagEntity::getTag)
                                .collect(Collectors.toList());

                        return new CoResumeSectionRequestDTO(
                                sectionEntity.getType(),
                                sectionEntity.getTitle(),
                                sectionEntity.getComment(),
                                sectionEntity.getContent(),
                                tagsForThisSection, // ✅ 섹션별 태그 포함
                                sectionEntity.isMultiSelect(),
                                sectionEntity.getConditions(),
                                sectionEntity.getDirectInputValue()
                        );
                    })
                    .collect(Collectors.toList());

            // 3️⃣ 희망직무 태그는 section 없이 저장됨
            List<String> jobTagStrings = allTags.stream()
                    .filter(tag -> tag.getSection() == null)
                    .map(CoResumeTagEntity::getTag)
                    .collect(Collectors.toList());

            // 4️⃣ DTO 리턴
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

        // ✅ 요기! 전송된 데이터 로그 찍기
        System.out.println("✅ [업데이트 요청] 제목: " + updatedResume.getTitle());
        for (CoResumeSectionRequestDTO section : updatedResume.getSections()) {
            System.out.println("🟢 섹션 제목: " + section.getTitle());
            System.out.println("📎 설명: " + section.getComment());
            System.out.println("🏷️ 태그: " + section.getTags());
            System.out.println("🧩 조건: " + section.getConditions());
            System.out.println("🔁 복수선택 여부: " + section.isMultiSelect());


        }

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

        resumeEntity.getSections().clear(); // ✅ 기존 리스트 clear
        resumeEntity.getSections().addAll(updatedSections); // ✅ 새로 받은 리스트 추가


        // 태그도 모두 새로 갱신
        List<CoResumeTagEntity> updatedTags = new ArrayList<>();

// 1️⃣ 희망직무 태그 저장 (resume만 연결)
        if (updatedResume.getJobTags() != null) {
            for (String tagValue : updatedResume.getJobTags()) {
                CoResumeTagEntity tag = new CoResumeTagEntity();
                tag.setTag(tagValue);
                tag.setResume(resumeEntity);
                updatedTags.add(tag);
            }
        }

// 2️⃣ 각 섹션 태그도 저장 (resume + section 연결)
        for (CoResumeSectionEntity section : updatedSections) {
            for (String tagValue : section.getTags()) {
                CoResumeTagEntity tag = new CoResumeTagEntity();
                tag.setTag(tagValue);
                tag.setResume(resumeEntity);
                tag.setSection(section);
                updatedTags.add(tag);
            }
        }

        // ✅ 여기 로그 추가!
        System.out.println("✅ 저장할 jobTags: " + updatedTags.size());
        System.out.println("✅ 저장할 sections: " + updatedSections.size());

        resumeEntity.getJobTags().clear();  // 기존 태그 리스트 비움
        resumeEntity.getJobTags().addAll(updatedTags);  // 새로 받은 태그들 추가



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
