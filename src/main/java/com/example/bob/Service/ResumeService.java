package com.example.bob.Service;

import com.example.bob.DTO.ResumeDTO;
import com.example.bob.DTO.ResumeSectionDTO;
import com.example.bob.DTO.ResumeSectionSubmitDTO;
import com.example.bob.DTO.ResumeSubmitRequestDTO;
import com.example.bob.DTO.UserProjectResponseDTO;
import com.example.bob.DTO.EducationDTO;
import com.example.bob.DTO.ResumeDetailDTO;
import com.example.bob.DTO.ResumeDetailSectionDTO;
import com.example.bob.DTO.ResumeDragItemDTO;



import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CoResumeSectionEntity;
import com.example.bob.Entity.CoResumeTagEntity;
import com.example.bob.Entity.ResumeEntity;
import com.example.bob.Entity.ResumeSectionEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.ResumeEducationEntity;
import com.example.bob.Entity.ResumeFileEntity;
import com.example.bob.Entity.ResumeDragItemEntity;
import com.example.bob.Entity.JobApplicationEntity;
import com.example.bob.Entity.JobApplicationStatus;
import com.example.bob.Entity.CoJobPostEntity;

import com.example.bob.Repository.CoResumeRepository;
import com.example.bob.Repository.CoResumeSectionRepository;
import com.example.bob.Repository.ResumeRepository;
import com.example.bob.Repository.ResumeSectionRepository;
import com.example.bob.Repository.UserProjectRepository;
import com.example.bob.Repository.ResumeEducationRepository;
import com.example.bob.Repository.ResumeFileRepository;
import com.example.bob.Repository.ResumeDragItemRepository;
import com.example.bob.Repository.CoJobPostRepository;
import com.example.bob.Repository.JobApplicationRepository;


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

    @Autowired
    private CoJobPostRepository coJobPostRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;


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
        // 1️⃣ 기업 이력서(CoResumeEntity) 조회
        CoResumeEntity coResume = coResumeRepository.findById(request.getCoResumeId())
                .orElseThrow(() -> new RuntimeException("해당 기업 이력서 양식을 찾을 수 없습니다."));

        // 🔄 지원할 공고(CoJobPostEntity) 조회
        CoJobPostEntity jobPost = coJobPostRepository.findById(request.getJobPostId())
                .orElseThrow(() -> new RuntimeException("공고가 존재하지 않습니다."));

        // 2️⃣ 사용자 이력서(ResumeEntity) 생성
        ResumeEntity resume = new ResumeEntity();
        resume.setCoResume(coResume);
        resume.setUser(user);
        resume.setSubmittedAt(new Date());

        // 3️⃣ 섹션 생성
        List<ResumeSectionEntity> sectionEntities = new ArrayList<>();
        for (ResumeSectionSubmitDTO dto : request.getSections()) {
            CoResumeSectionEntity coSection = coResumeSectionRepository.findById(dto.getCoSectionId())
                    .orElseThrow(() -> new RuntimeException("해당 섹션이 존재하지 않습니다."));

            ResumeSectionEntity section = new ResumeSectionEntity();
            section.setResume(resume);
            section.setCoSection(coSection);
            section.setContent(dto.getContent());
            section.setSelectedTags(dto.getSelectedTags());

            sectionEntities.add(section);
        }

        resume.setSections(sectionEntities);
        sectionEntities.forEach(sec -> sec.setResume(resume));

        // 4️⃣ 이력서 및 섹션 저장
        resumeRepository.save(resume);
        resumeSectionRepository.saveAll(sectionEntities);

        // 5️⃣ 학력 저장
        for (ResumeSectionSubmitDTO dto : request.getSections()) {
            if (dto.getEducations() != null && !dto.getEducations().isEmpty()) {
                ResumeSectionEntity targetSection = sectionEntities.stream()
                        .filter(sec -> sec.getCoSection().getId().equals(dto.getCoSectionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("매칭되는 섹션이 없습니다."));

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

                    resumeEducationRepository.save(edu);
                }
            }
        }

        // 6️⃣ 파일 첨부 저장
        for (ResumeSectionSubmitDTO dto : request.getSections()) {
            String uploaded = dto.getUploadedFileName();
            if (uploaded != null && !uploaded.isBlank()) {
                ResumeSectionEntity targetSection = sectionEntities.stream()
                        .filter(sec -> sec.getCoSection().getId().equals(dto.getCoSectionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("매칭되는 섹션이 없습니다."));

                ResumeFileEntity fileEntity = new ResumeFileEntity();
                fileEntity.setResumeSection(targetSection);
                fileEntity.setFileName(uploaded);

                resumeFileRepository.save(fileEntity);
            }
        }

        // 7️⃣ 드래그 항목 저장
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

        // 8️⃣ 지원 내역 저장 (JobApplicationEntity)
        JobApplicationEntity application = JobApplicationEntity.builder()
                .user(user)
                .resume(resume)
                .jobPost(jobPost)
                .appliedAt(new Date())
                .status(JobApplicationStatus.SUBMITTED)
                .build();

        jobApplicationRepository.save(application);
    }


    // ✅ 이력서 상세 조회: 공고 + 사용자 기준으로 가장 마지막 제출 이력서 반환
    public ResumeDetailDTO getResumeForJobPost(Long jobPostId, UserEntity user) {

        // 1️⃣ 지원 내역 중 가장 최근 이력서 조회
        JobApplicationEntity application = jobApplicationRepository
                .findTopByUserAndJobPost_IdOrderByAppliedAtDesc(user, jobPostId)
                .orElseThrow(() -> new RuntimeException("해당 공고에 제출한 이력서가 없습니다."));

        ResumeEntity resume = application.getResume();

        // 2️⃣ 상위 DTO 생성
        ResumeDetailDTO dto = new ResumeDetailDTO();
        dto.setTitle(resume.getCoResume().getTitle());

        // 3️⃣ 희망직무 태그
        dto.setJobTags(
                resume.getCoResume().getJobTags().stream()
                        .map(CoResumeTagEntity::getTag)
                        .collect(Collectors.toList())
        );

        // 4️⃣ 섹션 리스트 구성
        List<ResumeDetailSectionDTO> sections = new ArrayList<>();

        for (ResumeSectionEntity section : resume.getSections()) {
            ResumeDetailSectionDTO s = new ResumeDetailSectionDTO();

            s.setId(section.getId());
            s.setTitle(section.getCoSection().getTitle());
            s.setComment(section.getCoSection().getComment());
            s.setType(section.getCoSection().getType());
            s.setConditions(section.getCoSection().getConditions());
            s.setTags(section.getCoSection().getSectionTags().stream()
                    .map(CoResumeTagEntity::getTag)
                    .collect(Collectors.toList()));

            // ✅ 사용자 입력 내용
            s.setContent(section.getContent());
            s.setSelectedTags(section.getSelectedTags());

            // ✅ 학력
            List<ResumeEducationEntity> eduEntities = resumeEducationRepository.findByResumeSection(section);
            List<EducationDTO> eduDTOs = eduEntities.stream().map(e -> {
                EducationDTO edto = new EducationDTO();
                edto.setSchoolName(e.getSchoolName());
                edto.setMajorName(e.getMajorName());
                edto.setStatus(e.getStatus());
                edto.setStartYear(e.getStartYear());
                edto.setStartMonth(e.getStartMonth());
                edto.setEndYear(e.getEndYear());
                edto.setEndMonth(e.getEndMonth());
                return edto;
            }).collect(Collectors.toList());
            s.setEducations(eduDTOs);

            // ✅ 첨부 파일 리스트 처리
            List<ResumeFileEntity> fileEntities = resumeFileRepository.findByResumeSection(section);
            if (!fileEntities.isEmpty()) {
                List<String> filenames = fileEntities.stream()
                        .map(ResumeFileEntity::getFileName)
                        .collect(Collectors.toList());

                // ✅ 확인용 콘솔 로그
                System.out.println("📎 섹션 ID " + section.getId() + " / 타입: " + section.getCoSection().getType());
                System.out.println("📄 파일 첨부 리스트: " + filenames);

                s.setFileNames(filenames);
            }



            // ✅ 드래그 항목
            List<ResumeDragItemEntity> dragEntities = resumeDragItemRepository.findBySection(section);
            List<ResumeDragItemDTO> dragDTOs = dragEntities.stream().map(d -> {
                return new ResumeDragItemDTO(
                        section.getCoSection().getId(),
                        d.getItemType(),
                        d.getReferenceId(),
                        d.getDisplayText(),
                        d.getFilePath()
                );
            }).collect(Collectors.toList());
            s.setDragItems(dragDTOs);

            sections.add(s);
        }

        // 5️⃣ 섹션 DTO 넣기
        dto.setSections(sections);

        return dto;
    }



}
