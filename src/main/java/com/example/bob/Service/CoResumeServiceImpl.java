    package com.example.bob.Service;

    import com.example.bob.Controller.CoResumeEditController;
    import com.example.bob.DTO.CoResumeRequestDTO;
    import com.example.bob.DTO.CoResumeSectionRequestDTO;
    import com.example.bob.Entity.CoResumeEntity;
    import com.example.bob.Entity.CoResumeSectionEntity;
    import com.example.bob.Entity.CoResumeTagEntity;
    import com.example.bob.Repository.CoResumeRepository;
    import com.example.bob.Repository.CoResumeSectionRepository;
    import com.example.bob.Entity.CompanyEntity;
    import com.example.bob.Repository.CompanyRepository;


    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;
    import java.util.stream.Collectors;
    import java.util.Date;
    import jakarta.persistence.EntityManager;
    import jakarta.persistence.PersistenceContext;
    import jakarta.transaction.Transactional;



    @Service
    public class CoResumeServiceImpl implements CoResumeService {

        private final CoResumeRepository coResumeRepository;
        private final CoResumeSectionRepository coResumeSectionRepository;
        private static final Logger logger = LoggerFactory.getLogger(CoResumeEditController.class);
        private final CompanyRepository companyRepository;


        @PersistenceContext
        private EntityManager entityManager;

        @Autowired
        public CoResumeServiceImpl(CoResumeRepository coResumeRepository,
                                   CoResumeSectionRepository coResumeSectionRepository,
                                   CompanyRepository companyRepository) {
            this.coResumeRepository = coResumeRepository;
            this.coResumeSectionRepository = coResumeSectionRepository;
            this.companyRepository = companyRepository; // ✅ 주입 성공
        }


        // ✅ 이력서 저장 (제작)
        @Override
        public void saveResume(CoResumeRequestDTO requestDTO, Long companyId) {
            logger.info("이력서 저장 요청 - 제목: {}", requestDTO.getTitle());

            CoResumeEntity resume = new CoResumeEntity();
            resume.setTitle(requestDTO.getTitle());
            resume.setCreatedAt(requestDTO.getCreatedAt() != null ? requestDTO.getCreatedAt() : new Date());  // 🔥 추가
            CompanyEntity company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회사 없음: " + companyId));
            resume.setCompany(company);  // ✅ 여기에 회사 연결




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
                                    sectionEntity.getId(),
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

                // ✅ 희망직무 섹션 중복 방지 조건 추가
                boolean hasJobSection = sectionDTOList.stream()
                        .anyMatch(section -> "희망직무".equals(section.getTitle()));

               // ✅ 중복되지 않은 경우에만 섹션에 추가
                if (!jobTagStrings.isEmpty() && !hasJobSection) {
                    CoResumeSectionRequestDTO jobSection = new CoResumeSectionRequestDTO(
                            null, // ❗ id 없음 (DB에 없는 가상 섹션이므로 null로 설정)
                            "선택형",
                            "희망직무",
                            "희망하는 직무를 선택해주세요.",
                            "",
                            jobTagStrings,
                            true,
                            new ArrayList<>(),
                            null
                    );
                    sectionDTOList.add(jobSection);
                }



                // 5️⃣ DTO 리턴
                return new CoResumeRequestDTO(
                        resumeEntity.getTitle(),
                        sectionDTOList,
                        resumeEntity.getCreatedAt(),
                        jobTagStrings // 👉 DTO는 그대로 유지
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

            // 기존 섹션과 태그 초기화
            resumeEntity.getSections().clear();
            resumeEntity.getJobTags().clear();

            List<CoResumeSectionEntity> updatedSections = new ArrayList<>();
            List<CoResumeTagEntity> updatedTags = new ArrayList<>();

            if (updatedResume.getSections() != null && !updatedResume.getSections().isEmpty()) {
                for (CoResumeSectionRequestDTO sectionDTO : updatedResume.getSections()) {
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

                    updatedSections.add(sectionEntity);

                    // 태그 처리
                    if (sectionDTO.getTags() != null) {
                        for (String tagValue : sectionDTO.getTags()) {
                            CoResumeTagEntity tag = new CoResumeTagEntity();
                            tag.setTag(tagValue);
                            tag.setResume(resumeEntity);
                            tag.setSection(sectionEntity);  // ✅ 섹션과 연결
                            updatedTags.add(tag);
                        }
                    }
                }

                // 섹션 저장
                resumeEntity.getSections().addAll(updatedSections);
            }

            // ✅ jobTags 처리 (희망직무만 따로 저장하지 않고도 위에서 다 저장됨)
            // 👉 그래도 프론트에서 jobTags만 따로 쓰는 경우가 있다면 유지
            if (updatedResume.getJobTags() != null) {
                for (String tagValue : updatedResume.getJobTags()) {
                    // 희망직무는 section 없이 저장
                    CoResumeTagEntity tag = new CoResumeTagEntity();
                    tag.setTag(tagValue);
                    tag.setResume(resumeEntity);
                    updatedTags.add(tag);
                }
            }

            // 태그 최종 반영
            resumeEntity.getJobTags().addAll(updatedTags);

            coResumeRepository.save(resumeEntity);
        }

        @Override
        public List<CoResumeEntity> getResumesByCompanyId(Long companyId) {
            return coResumeRepository.findByCompany_CompanyId(companyId);
        }


        // ✅ 이력서 전체 목록 조회
        @Override
        public List<CoResumeEntity> getAllResumes() {
            return coResumeRepository.findAll();
        }

        // ✅ 이력서 삭제
        @Override
        @Transactional
        public void deleteResume(Long id) {
            logger.info("🗑️ 기업 이력서 삭제 시작 - ID: {}", id);

            // 0-1️⃣ my_resume_id 기준 job_application_entity 삭제도 추가
            int deletedMyResumeApplications = entityManager.createNativeQuery("""
    DELETE FROM job_application_entity
    WHERE my_resume_id IN (
        SELECT mr.id FROM my_resume mr WHERE mr.co_resume_id = ?
    )
""").setParameter(1, id).executeUpdate();
            logger.info("🔸 my_resume 기반 지원 내역 삭제 완료 ({}건)", deletedMyResumeApplications);


            // 1️⃣ job_resume 삭제
            int deletedJobResume = entityManager.createNativeQuery("""
        DELETE FROM job_resume WHERE resume_id = ?
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 job_resume 삭제 완료 ({}건)", deletedJobResume);

            // 2️⃣ resume_section_entity_selected_tags 삭제
            int deletedTags = entityManager.createNativeQuery("""
        DELETE t FROM resume_section_entity_selected_tags t
        WHERE t.resume_section_entity_id IN (
            SELECT s.id FROM resume_section_entity s
            WHERE s.co_section_id IN (
                SELECT c.id FROM co_resume_section_entity c WHERE c.resume_id = ?
            )
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 resume_section_entity_selected_tags 삭제 완료 ({}건)", deletedTags);

            // 3️⃣ resume_drag_item_entity 삭제
            int deletedDragItems = entityManager.createNativeQuery("""
        DELETE d FROM resume_drag_item_entity d
        WHERE d.section_id IN (
            SELECT s.id FROM resume_section_entity s
            WHERE s.co_section_id IN (
                SELECT c.id FROM co_resume_section_entity c WHERE c.resume_id = ?
            )
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 resume_drag_item_entity 삭제 완료 ({}건)", deletedDragItems);

            // 4️⃣ resume_education_entity 삭제
            int deletedEdu = entityManager.createNativeQuery("""
        DELETE e FROM resume_education_entity e
        WHERE e.resume_section_id IN (
            SELECT s.id FROM resume_section_entity s
            WHERE s.co_section_id IN (
                SELECT c.id FROM co_resume_section_entity c WHERE c.resume_id = ?
            )
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 resume_education_entity 삭제 완료 ({}건)", deletedEdu);

            // 5️⃣ resume_file_entity 삭제
            int deletedFiles = entityManager.createNativeQuery("""
        DELETE f FROM resume_file_entity f
        WHERE f.resume_section_id IN (
            SELECT s.id FROM resume_section_entity s
            WHERE s.co_section_id IN (
                SELECT c.id FROM co_resume_section_entity c WHERE c.resume_id = ?
            )
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 resume_file_entity 삭제 완료 ({}건)", deletedFiles);

            // ⚡ 5.5️⃣ resume_career_entity 삭제
            int deletedCareer = entityManager.createNativeQuery("""
        DELETE c FROM resume_career_entity c
        WHERE c.resume_section_id IN (
            SELECT s.id FROM resume_section_entity s
            WHERE s.co_section_id IN (
                SELECT c2.id FROM co_resume_section_entity c2 WHERE c2.resume_id = ?
            )
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 resume_career_entity 삭제 완료 ({}건)", deletedCareer);

            // ⚡ 5.6️⃣ resume_portfolio_entity 삭제 (새 추가)
            int deletedPortfolio = entityManager.createNativeQuery("""
        DELETE p FROM resume_portfolio_entity p
        WHERE p.resume_section_id IN (
            SELECT s.id FROM resume_section_entity s
            WHERE s.co_section_id IN (
                SELECT c2.id FROM co_resume_section_entity c2 WHERE c2.resume_id = ?
            )
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 resume_portfolio_entity 삭제 완료 ({}건)", deletedPortfolio);

            // 6️⃣ resume_section_entity 삭제
            int deletedUserSections = entityManager.createNativeQuery("""
        DELETE rse FROM resume_section_entity rse
        WHERE rse.co_section_id IN (
            SELECT s.id FROM co_resume_section_entity s WHERE s.resume_id = ?
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 resume_section_entity 삭제 완료 ({}건)", deletedUserSections);

            // 7️⃣ resume_entity 삭제
            int deletedUserResumes = entityManager.createNativeQuery("""
        DELETE FROM resume_entity WHERE co_resume_id = ?
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 resume_entity 삭제 완료 ({}건)", deletedUserResumes);

            // 8️⃣ co_resume_section_entity_conditions 삭제
            int deletedConditions = entityManager.createNativeQuery("""
        DELETE c FROM co_resume_section_entity_conditions c
        WHERE c.co_resume_section_entity_id IN (
            SELECT s.id FROM co_resume_section_entity s WHERE s.resume_id = ?
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 co_resume_section_entity_conditions 삭제 완료 ({}건)", deletedConditions);

            // 9️⃣ co_resume_tag_entity 삭제
            int deletedTagsEntity = entityManager.createNativeQuery("""
        DELETE t FROM co_resume_tag_entity t
        WHERE t.section_id IN (
            SELECT s.id FROM co_resume_section_entity s WHERE s.resume_id = ?
        )
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 co_resume_tag_entity 삭제 완료 ({}건)", deletedTagsEntity);

            // 🔟 co_resume_section_entity 삭제
            int deletedCoSections = entityManager.createNativeQuery("""
        DELETE FROM co_resume_section_entity WHERE resume_id = ?
    """).setParameter(1, id).executeUpdate();
            logger.info("🔸 co_resume_section_entity 삭제 완료 ({}건)", deletedCoSections);

            // 11️⃣ co_resume_entity 삭제
            coResumeRepository.deleteById(id);
            logger.info("✅ co_resume_entity 삭제 완료 - ID: {}", id);
        }


    }
