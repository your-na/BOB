package com.example.bob.Service; // 실제 기능을 "구현"하는 클래스 (기업 이력서 저장)

import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.DTO.CoResumeSectionRequestDTO;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CoResumeSectionEntity;
import com.example.bob.Repository.CoResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CoResumeServiceImpl implements CoResumeService {

    private final CoResumeRepository coResumeRepository;

    @Autowired
    public CoResumeServiceImpl(CoResumeRepository coResumeRepository) {
        this.coResumeRepository = coResumeRepository;
    }

    @Override
    public void saveResume(CoResumeRequestDTO requestDTO) {
        // 📝 이력서 엔티티 생성
        CoResumeEntity resume = new CoResumeEntity();
        resume.setTitle(requestDTO.getTitle());

        // 📝 섹션들 매핑해서 이력서에 추가
        if (requestDTO.getSections() != null) {
            for (CoResumeSectionRequestDTO sectionDTO : requestDTO.getSections()) {
                CoResumeSectionEntity section = new CoResumeSectionEntity();
                section.setType(sectionDTO.getType());
                section.setTitle(sectionDTO.getTitle());
                section.setComment(sectionDTO.getComment());
                section.setContent(sectionDTO.getContent());
                section.setTags(sectionDTO.getTags());

                // 연관관계 설정
                resume.addSection(section);
            }
        }

        // ✅ DB에 저장
        coResumeRepository.save(resume);
    }
}
