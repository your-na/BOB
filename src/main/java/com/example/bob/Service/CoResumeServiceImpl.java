package com.example.bob.Service; // 실제 기능을 "구현"하는 클래스 (기업 이력서 저장)

import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.DTO.CoResumeSectionRequestDTO;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CoResumeSectionEntity;
import com.example.bob.Repository.CoResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.text.SimpleDateFormat;

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

        // 작성일 설정 (requestDTO에 작성일이 있으면 그 값을 사용, 없으면 현재 시간으로 설정)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (requestDTO.getCreatedAt() != null) {
            // 클라이언트가 보낸 작성일 사용, 년-월-일 형식으로 포맷
            String formattedDate = sdf.format(requestDTO.getCreatedAt());  // 포맷팅된 날짜 문자열
            resume.setCreatedAt(formattedDate);  // String 타입으로 저장
        } else {
            // 작성일이 없으면 현재 날짜를 년-월-일 형식으로 설정
            String formattedDate = sdf.format(new Date());
            resume.setCreatedAt(formattedDate);  // String 타입으로 저장
        }

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
