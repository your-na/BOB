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
import com.example.bob.DTO.JobHistoryDTO;




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
import com.example.bob.Entity.ProjectHistoryEntity;
import com.example.bob.Entity.ResumeCareerEntity;
import com.example.bob.Entity.ResumePortfolioEntity;

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
import com.example.bob.Repository.ResumeCareerRepository;
import com.example.bob.Repository.ResumePortfolioRepository;
import com.example.bob.Repository.ContestHistoryRepository;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;




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

    @Autowired
    private com.example.bob.Repository.ProjectHistoryRepository projectHistoryRepository;

    @Autowired
    private ResumeCareerRepository resumeCareerRepository;

    @Autowired
    private ResumePortfolioRepository resumePortfolioRepository;

    @Autowired
    private ContestHistoryRepository contestHistoryRepository;




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
    @Transactional(readOnly = true)
    public List<UserProjectResponseDTO> getCompletedProjectsForUser(Long userId) {
        return userProjectRepository
                .findByUser_UserIdAndStatusAndSubmittedFileNameIsNotNullAndVisibleTrue(userId, "완료")
                .stream()
                .map(up -> {
                    Long projectId = up.getProject().getId(); // ✅ 프로젝트 ID 추출
                    String title = up.getProject().getTitle();
                    String submittedDate = up.getSubmissionDate() != null ? up.getSubmissionDate().toString() : "제출일 없음";
                    String startDate = up.getProject().getStartDate() != null ? up.getProject().getStartDate().toString() : "시작일 없음";
                    String endDate = up.getProject().getEndDate() != null ? up.getProject().getEndDate().toString() : "종료일 없음";
                    String submittedFileName = up.getSubmittedFileName();
                    String filePath = (submittedFileName != null && !submittedFileName.isEmpty())
                            ? "/download/" + submittedFileName
                            : null;

                    return new UserProjectResponseDTO(
                            projectId,
                            title,
                            submittedDate,
                            startDate,
                            endDate,
                            submittedFileName,
                            filePath
                    );
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public void submitUserResume(ResumeSubmitRequestDTO request, UserEntity user) {

        // 🔒 중복 지원 체크
        boolean alreadyApplied = jobApplicationRepository
                .existsByUserAndJobPost_IdAndStatus(user, request.getJobPostId(), JobApplicationStatus.SUBMITTED);
        if (alreadyApplied) {
            throw new IllegalStateException("이미 이 공고에 지원한 이력이 있습니다.");
        }

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
        resume.setJobPost(jobPost);

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

        // 5-2️⃣ 경력 저장
        for (ResumeSectionSubmitDTO dto : request.getSections()) {
            if (dto.getCareers() != null && !dto.getCareers().isEmpty()) {
                ResumeSectionEntity targetSection = sectionEntities.stream()
                        .filter(sec -> sec.getCoSection().getId().equals(dto.getCoSectionId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("매칭되는 섹션이 없습니다."));

                dto.getCareers().forEach(careerDTO -> {
                    ResumeCareerEntity career = new ResumeCareerEntity();
                    career.setResumeSection(targetSection);

                    // ✅ JobHistoryDTO 기준 필드 매핑
                    career.setCompanyName(careerDTO.getWorkplace()); // 회사명
                    career.setPosition(careerDTO.getJobTitle());     // 직무명
                    career.setStatus(careerDTO.getStatus());         // 재직/퇴사 상태

                    // ✅ LocalDate → 연/월 분리 저장
                    if (careerDTO.getStartDate() != null) {
                        career.setStartYear(String.valueOf(careerDTO.getStartDate().getYear()));
                        career.setStartMonth(String.format("%02d", careerDTO.getStartDate().getMonthValue()));
                    }

                    if (careerDTO.getEndDate() != null) {
                        career.setEndYear(String.valueOf(careerDTO.getEndDate().getYear()));
                        career.setEndMonth(String.format("%02d", careerDTO.getEndDate().getMonthValue()));
                    }

                    resumeCareerRepository.save(career);
                });
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

                    // ✅ [2️⃣ 추가] 포트폴리오 저장 (PROJECT / CONTEST 구분)
                    if ("PROJECT".equalsIgnoreCase(dragDTO.getItemType())) {
                        userProjectRepository.findById(dragDTO.getReferenceId()).ifPresent(project -> {
                            ResumePortfolioEntity pf = new ResumePortfolioEntity();
                            pf.setResumeSection(targetSection);
                            pf.setType("PROJECT");
                            pf.setTitle(project.getProject().getTitle());
                            pf.setStatus("완료");
                            pf.setSubmittedFile(project.getSubmittedFileName());

                            if (project.getProject().getStartDate() != null) {
                                pf.setStartYear(String.valueOf(project.getProject().getStartDate().getYear()));
                                pf.setStartMonth(String.format("%02d", project.getProject().getStartDate().getMonthValue()));
                            }
                            if (project.getProject().getEndDate() != null) {
                                pf.setEndYear(String.valueOf(project.getProject().getEndDate().getYear()));
                                pf.setEndMonth(String.format("%02d", project.getProject().getEndDate().getMonthValue()));
                            }

                            resumePortfolioRepository.save(pf);
                        });
                    }


                    else if ("CONTEST".equalsIgnoreCase(dragDTO.getItemType())) {
                        contestHistoryRepository.findById(dragDTO.getReferenceId()).ifPresent(contest -> {
                            ResumePortfolioEntity pf = new ResumePortfolioEntity();
                            pf.setResumeSection(targetSection);
                            pf.setType("CONTEST");
                            pf.setTitle(contest.getTitle());
                            pf.setStatus(contest.getStatus());

                            if (contest.getStartDate() != null) {
                                pf.setStartYear(String.valueOf(contest.getStartDate().getYear()));
                                pf.setStartMonth(String.format("%02d", contest.getStartDate().getMonthValue()));
                            }
                            if (contest.getEndDate() != null) {
                                pf.setEndYear(String.valueOf(contest.getEndDate().getYear()));
                                pf.setEndMonth(String.format("%02d", contest.getEndDate().getMonthValue()));
                            }

                            resumePortfolioRepository.save(pf);
                        });
                    }

                });
                System.out.println("📦 [DEBUG] 드래그 항목 감지됨: " + dto.getDragItems().size());

                dto.getDragItems().forEach(dragDTO -> {
                    System.out.println("➡️ [DEBUG] 드래그 DTO 타입: " + dragDTO.getItemType() +
                            ", referenceId: " + dragDTO.getReferenceId() +
                            ", displayText: " + dragDTO.getDisplayText() +
                            ", filePath: " + dragDTO.getFilePath());

                    ResumeDragItemEntity drag = new ResumeDragItemEntity();
                    drag.setSection(targetSection);
                    drag.setItemType(dragDTO.getItemType());
                    drag.setReferenceId(dragDTO.getReferenceId());
                    drag.setDisplayText(dragDTO.getDisplayText());
                    drag.setFilePath(dragDTO.getFilePath());

                    resumeDragItemRepository.save(drag);
                    System.out.println("💾 [DEBUG] 저장 완료됨 → ID: " + drag.getId());
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
        dto.setId(resume.getId());
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


                s.setFileNames(filenames);
            }



            // ✅ 드래그 항목
            List<ResumeDragItemEntity> dragEntities = resumeDragItemRepository.findBySection(section);
            List<ResumeDragItemDTO> dragDTOs = dragEntities.stream().map(d -> {
                ResumeDragItemDTO itemDto = new ResumeDragItemDTO(
                        section.getCoSection().getId(),
                        d.getItemType(),
                        d.getReferenceId(),
                        d.getDisplayText(),
                        d.getFilePath()
                );

                // 🔥 프로젝트인 경우 날짜 가져오기
                if ("PROJECT".equalsIgnoreCase(d.getItemType())) {
                    // ✅ 프로젝트 ID로 히스토리 목록 조회 (최신 순)
                    List<ProjectHistoryEntity> histories = projectHistoryRepository
                            .findByProjectIdOrderByModifiedAtDesc(d.getReferenceId());

                    if (!histories.isEmpty()) {
                        ProjectHistoryEntity ph = histories.get(0); // 🔹 최신 히스토리 가져오기
                        itemDto.setStartDate(ph.getStartDate().toString()); // 🔹 시작일 설정

                        // ✅ 제출일 가져오기 (없으면 종료일 사용)
                        userProjectRepository.findByProject_IdAndUser_UserId(ph.getProject().getId(), user.getUserId())
                                .ifPresentOrElse(
                                        up -> {
                                            if (up.getSubmissionDate() != null) {
                                                itemDto.setEndDate(up.getSubmissionDate().toString()); // 🔹 제출일 사용
                                            } else {
                                                itemDto.setEndDate(ph.getEndDate().toString()); // 🔹 백업으로 종료일 사용
                                            }
                                        },
                                        () -> itemDto.setEndDate(ph.getEndDate().toString()) // 🔹 해당 유저 정보 없을 경우
                                );
                    }
                }



                return itemDto;
            }).collect(Collectors.toList());

            s.setDragItems(dragDTOs);

            // ✅ 드래그 항목이 있으면 content 제거
            if (!dragDTOs.isEmpty()) {
                s.setContent(null);
            }
            sections.add(s);

        }

        // 5️⃣ 섹션 DTO 넣기
        dto.setSections(sections);

        // ✅ 나이 계산 추가
        if (user.getBirthday() != null && !user.getBirthday().isEmpty()) {
            try {
                int age = Period.between(LocalDate.parse(user.getBirthday()), LocalDate.now()).getYears();
                dto.setAge(age); // 👉 DTO에 age 필드 필요
            } catch (Exception e) {
                dto.setAge(0); // 파싱 실패 시 예외 처리
            }
        }


        // 6️⃣ 사용자 정보 추가
        dto.setUserName(user.getUserName());
        dto.setUserNick(user.getUserNick());
        dto.setUserEmail(user.getUserEmail());
        dto.setUserPhone(user.getUserPhone());
        dto.setSex(user.getSex());
        dto.setBirthday(user.getBirthday());
        dto.setRegion(user.getRegion());
        dto.setMainLanguage(user.getMainLanguage());
        dto.setProfileImageUrl(user.getProfileImageUrl());


        return dto;
    }

    // ✅ 가장 최근 지원 내역만 취소 처리
    @Transactional
    public boolean cancelJobApplication(Long jobPostId, UserEntity user) {
        // 가장 최근 SUBMITTED 상태 지원 내역만 조회
        Optional<JobApplicationEntity> optionalApp =
                jobApplicationRepository.findTopByUserAndJobPost_IdAndStatusOrderByAppliedAtDesc(
                        user, jobPostId, JobApplicationStatus.SUBMITTED);

        if (optionalApp.isPresent()) {
            JobApplicationEntity app = optionalApp.get();
            app.setStatus(JobApplicationStatus.CANCELED); // 상태 변경
            return true;
        }

        return false; // 지원 내역 없거나 이미 취소된 경우
    }

    // ✅ 기업용: 특정 공고에 제출된 가장 최근 이력서 조회
    public ResumeDetailDTO getResumeForCompany(Long jobPostId) {
        // 1️⃣ 공고에 제출된 이력서 중 가장 최근 지원 내역
        JobApplicationEntity application = jobApplicationRepository
                .findTopByJobPost_IdAndStatusOrderByAppliedAtDesc(
                        jobPostId, JobApplicationStatus.SUBMITTED)
                .orElseThrow(() -> new RuntimeException("해당 공고에 제출된 이력서가 없습니다."));

        // 2️⃣ 사용자 기준으로 기존 getResumeForJobPost 재사용
        return getResumeForJobPost(jobPostId, application.getUser());
    }

    // ✅ 기업 사용자가 이력서 ID만으로 상세 이력서를 조회할 수 있게 함
    public ResumeDetailDTO getResumeForCompanyWithResumeId(Long resumeId) {

        // 1️⃣ 이력서 ID로 이력서 조회
        ResumeEntity resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("해당 이력서가 없습니다."));

        // 2️⃣ 이력서 작성자
        UserEntity user = resume.getUser();

        // 3️⃣ 공고 정보 조회 (jobPost가 null이면 JobApplication에서 찾음)
        CoJobPostEntity jobPost = resume.getJobPost();
        if (jobPost == null) {
            JobApplicationEntity application = jobApplicationRepository
                    .findTopByResumeOrderByAppliedAtDesc(resume)
                    .orElseThrow(() -> new RuntimeException("이력서에 연결된 지원 정보가 없습니다."));

            jobPost = application.getJobPost();

            // 🔄 연결 정보 보완 (선택: DB 반영)
            resume.setJobPost(jobPost);
            resumeRepository.save(resume);
        }

        // 4️⃣ 기존 메서드 재활용
        return getResumeForJobPost(jobPost.getId(), user);
    }


    public ResumeDTO getPreviewResume(HttpSession session) {
        Object obj = session.getAttribute("previewResume");
        if (obj instanceof ResumeDTO resume) {
            return resume;
        }
        return null;
    }








}