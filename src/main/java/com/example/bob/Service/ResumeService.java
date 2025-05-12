package com.example.bob.Service;

import com.example.bob.DTO.ResumeDTO;
import com.example.bob.DTO.ResumeSectionDTO;
import com.example.bob.DTO.ResumeSectionSubmitDTO;
import com.example.bob.DTO.ResumeSubmitRequestDTO;
import com.example.bob.DTO.UserProjectResponseDTO;
import com.example.bob.DTO.EducationDTO;


import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CoResumeSectionEntity;
import com.example.bob.Entity.CoResumeTagEntity;
import com.example.bob.Entity.ResumeEntity;
import com.example.bob.Entity.ResumeSectionEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.ResumeEducationEntity;
import com.example.bob.Entity.ResumeFileEntity;
import com.example.bob.Entity.ResumeDragItemEntity;

import com.example.bob.Repository.CoResumeRepository;
import com.example.bob.Repository.CoResumeSectionRepository;
import com.example.bob.Repository.ResumeRepository;
import com.example.bob.Repository.ResumeSectionRepository;
import com.example.bob.Repository.UserProjectRepository;
import com.example.bob.Repository.ResumeEducationRepository;
import com.example.bob.Repository.ResumeFileRepository;
import com.example.bob.Repository.ResumeDragItemRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ResumeService {

    @Autowired
    private CoResumeRepository coResumeRepository;

    @Autowired
    private CoResumeSectionRepository coResumeSectionRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeSectionRepository resumeSectionRepository;

    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private ResumeEducationRepository resumeEducationRepository;

    @Autowired
    private ResumeFileRepository resumeFileRepository;

    @Autowired
    private ResumeDragItemRepository resumeDragItemRepository;

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
                    dto.setId(section.getId());
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

    // ✅ 사용자가 제출한 완료 프로젝트 목록 조회 (이력서 우측 탭에 사용됨)
    public List<UserProjectResponseDTO> getCompletedProjectsForResume(UserEntity user) {
        return userProjectRepository
                .findByUser_UserIdAndStatusAndSubmittedFileNameIsNotNullAndVisibleTrue(user.getUserId(), "완료")
                .stream()
                .map(up -> new UserProjectResponseDTO(
                        up.getProject().getTitle(),
                        up.getSubmissionDate() != null ? up.getSubmissionDate().toString() : "제출일 없음"
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void submitUserResume(ResumeSubmitRequestDTO request, UserEntity user) {
        // 1️⃣ 기반이 되는 기업 이력서(CoResumeEntity) 조회
        CoResumeEntity coResume = coResumeRepository.findById(request.getCoResumeId())
                .orElseThrow(() -> new RuntimeException("해당 기업 이력서 양식을 찾을 수 없습니다."));

        // 2️⃣ 이력서(ResumeEntity) 생성
        ResumeEntity resume = new ResumeEntity();
        resume.setCoResume(coResume);
        resume.setUser(user);
        resume.setSubmittedAt(new Date());

        // 3️⃣ 각 섹션을 저장할 리스트 생성
        List<ResumeSectionEntity> sectionEntities = new ArrayList<>();

        for (ResumeSectionSubmitDTO dto : request.getSections()) {
            // 3-1️⃣ CoResumeSectionEntity 조회
            CoResumeSectionEntity coSection = coResumeSectionRepository.findById(dto.getCoSectionId())
                    .orElseThrow(() -> new RuntimeException("해당 섹션이 존재하지 않습니다."));

            // 3-2️⃣ ResumeSectionEntity 생성
            ResumeSectionEntity section = new ResumeSectionEntity();
            section.setResume(resume);
            section.setCoSection(coSection);
            section.setContent(dto.getContent());
            section.setSelectedTags(dto.getSelectedTags());
            sectionEntities.add(section);
        }

        // 4️⃣ 이력서와 섹션들 연결
        resume.setSections(sectionEntities);
        for (ResumeSectionEntity sec : sectionEntities) {
            sec.setResume(resume);
        }

        // 5️⃣ 이력서 저장 (Cascade 적용 X)
        resumeRepository.save(resume);
        resumeSectionRepository.saveAll(sectionEntities);

        // 6️⃣ 학력사항 저장 (각 ResumeSection과 연결됨)
        for (ResumeSectionSubmitDTO dto : request.getSections()) {
            if (dto.getEducations() != null && !dto.getEducations().isEmpty()) {
                // 현재 섹션의 ResumeSectionEntity 찾기
                ResumeSectionEntity targetSection = sectionEntities.stream()
                        .filter(sec -> sec.getCoSection().getId().equals(dto.getCoSectionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("매칭되는 섹션이 없습니다."));

                // 각 학력 항목 저장
                for (EducationDTO eduDTO : dto.getEducations()) {
                    ResumeEducationEntity edu = new ResumeEducationEntity();
                    edu.setResumeSection(targetSection);
                    edu.setSchoolName(eduDTO.getSchoolName());
                    edu.setMajorName(eduDTO.getMajorName());
                    edu.setStatus(eduDTO.getStatus());
                    edu.setStartYear(eduDTO.getStartYear());
                    edu.setStartMonth(eduDTO.getStartMonth());
                    edu.setEndYear(eduDTO.getEndYear());
                    edu.setEndMonth(eduDTO.getEndMonth());

                    resumeEducationRepository.save(edu); // DB 저장
                }
            }
        }
        // 7️⃣ 파일/사진 첨부 저장 (uploadedFileName이 존재할 경우만 저장)
        for (ResumeSectionSubmitDTO dto : request.getSections()) {
            String uploaded = dto.getUploadedFileName();
            if (uploaded != null && !uploaded.isBlank()) {
                ResumeSectionEntity targetSection = sectionEntities.stream()
                        .filter(sec -> sec.getCoSection().getId().equals(dto.getCoSectionId()))
                          .findFirst()
                        .orElseThrow(() -> new RuntimeException("매칭되는 섹션이 없습니다."));

                ResumeFileEntity fileEntity = new ResumeFileEntity();
                fileEntity.setResumeSection(targetSection);
                fileEntity.setFileName(uploaded);  // 업로드된 실제 파일명

                resumeFileRepository.save(fileEntity);
            }
        }
        // 8️⃣ 드래그 항목 저장
        for (ResumeSectionSubmitDTO dto : request.getSections()) {
            if (dto.getDragItems() != null && !dto.getDragItems().isEmpty()) {
                ResumeSectionEntity targetSection = sectionEntities.stream()
                        .filter(sec -> sec.getCoSection().getId().equals(dto.getCoSectionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("매칭되는 섹션이 없습니다."));

                dto.getDragItems().forEach(dragDTO -> {
                    ResumeDragItemEntity drag = new ResumeDragItemEntity();
                    drag.setSection(targetSection);
                    drag.setItemType(dragDTO.getItemType());
                    drag.setReferenceId(dragDTO.getReferenceId());
                    drag.setDisplayText(dragDTO.getDisplayText());
                    drag.setFilePath(dragDTO.getFilePath());

                    resumeDragItemRepository.save(drag);
                });
            }
        }
    }






}
