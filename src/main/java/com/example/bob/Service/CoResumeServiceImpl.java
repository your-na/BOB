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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class CoResumeServiceImpl implements CoResumeService {

    private final CoResumeRepository coResumeRepository;
    private final CoResumeSectionRepository coResumeSectionRepository; // 이 부분 추가
    private static final Logger logger = LoggerFactory.getLogger(CoResumeEditController.class);  // 로그 출력을 위한 로거 설정

    // 생성자에 coResumeSectionRepository 추가
    @Autowired
    public CoResumeServiceImpl(CoResumeRepository coResumeRepository, CoResumeSectionRepository coResumeSectionRepository) {
        this.coResumeRepository = coResumeRepository;
        this.coResumeSectionRepository = coResumeSectionRepository;
    }

    public void saveResume(CoResumeRequestDTO requestDTO) {
        CoResumeEntity resume = new CoResumeEntity();
        resume.setTitle(requestDTO.getTitle());

        // 섹션들 추가
        if (requestDTO.getSections() != null) {
            for (CoResumeSectionRequestDTO sectionDTO : requestDTO.getSections()) {
                CoResumeSectionEntity sectionEntity = new CoResumeSectionEntity();
                sectionEntity.setType(sectionDTO.getType());
                sectionEntity.setTitle(sectionDTO.getTitle());
                sectionEntity.setComment(sectionDTO.getComment());
                sectionEntity.setContent(sectionDTO.getContent());
                sectionEntity.setTags(sectionDTO.getTags());

                // 연관 관계 설정
                resume.addSection(sectionEntity); // 이걸 통해 resume_id가 자동으로 설정됨
            }
        }

        coResumeRepository.save(resume);  // 이력서 저장
    }



        // ✅ 목록 조회용 메서드
        @Override
        public List<CoResumeEntity> getAllResumes() {
            return coResumeRepository.findAll();
        }

    // ✅ 삭제 메서드
    @Override
    public void deleteResume(Long id) {
        coResumeRepository.deleteById(id);
    }

    // ✅ 이력서 조회: 수정 페이지에서 사용할 데이터 반환
    @Override
    public CoResumeRequestDTO getResumeById(Long id) {
        logger.info("이력서 데이터 불러오기 요청 - ID: {}", id);  // 요청 로그 추가

        Optional<CoResumeEntity> resumeEntityOpt = coResumeRepository.findById(id);
        if (resumeEntityOpt.isPresent()) {
            CoResumeEntity resumeEntity = resumeEntityOpt.get();

            logger.info("이력서 데이터 불러오기 성공 - ID: {}", id);  // 성공 로그 추가
            logger.info("불러온 섹션 데이터: {}", resumeEntity.getSections());  // 섹션 데이터 확인

            List<CoResumeSectionRequestDTO> sectionDTOList = resumeEntity.getSections().stream()
                    .map(sectionEntity -> new CoResumeSectionRequestDTO(
                            sectionEntity.getType(),
                            sectionEntity.getTitle(),
                            sectionEntity.getComment(),
                            sectionEntity.getContent(),
                            sectionEntity.getTags() != null ? sectionEntity.getTags() : new ArrayList<String>()
                    ))
                    .collect(Collectors.toList());

            logger.info("변환된 이력서 데이터: {}", sectionDTOList);  // 변환된 섹션 데이터 확인

            return new CoResumeRequestDTO(
                    resumeEntity.getTitle(),
                    sectionDTOList,
                    resumeEntity.getCreatedAt()
            );
        } else {
            logger.error("이력서를 찾을 수 없습니다 - ID: {}", id);  // 에러 로그 추가
            throw new RuntimeException("이력서를 찾을 수 없습니다.");
        }
    }




    // ✅ 이력서 데이터를 업데이트하는 메서드
    @Override
    public void updateResume(Long id, CoResumeRequestDTO updatedResume) {
        // 이력서 엔티티를 DB에서 찾기
        CoResumeEntity resumeEntity = coResumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이력서가 존재하지 않습니다."));

        // 제목 및 수정일 업데이트
        resumeEntity.setTitle(updatedResume.getTitle());
        resumeEntity.setCreatedAt(updatedResume.getCreatedAt());  // 날짜 처리도 함께

        // 섹션 업데이트
        List<CoResumeSectionEntity> updatedSections = updatedResume.getSections().stream()
                .map(sectionDTO -> {
                    CoResumeSectionEntity sectionEntity = new CoResumeSectionEntity();
                    sectionEntity.setTitle(sectionDTO.getTitle());
                    sectionEntity.setType(sectionDTO.getType());
                    sectionEntity.setComment(sectionDTO.getComment());
                    sectionEntity.setContent(sectionDTO.getContent());
                    sectionEntity.setTags(sectionDTO.getTags()); // 선택형 태그 목록 설정

                    // 연관된 이력서 설정
                    sectionEntity.setResume(resumeEntity);

                    return sectionEntity;
                })
                .collect(Collectors.toList());

        // 기존 섹션 삭제 후 새로운 섹션 저장
        coResumeSectionRepository.deleteAll(resumeEntity.getSections());
        resumeEntity.setSections(updatedSections);

        coResumeRepository.save(resumeEntity);
    }

}
