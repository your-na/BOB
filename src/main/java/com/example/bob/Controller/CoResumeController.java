package com.example.bob.Controller;

import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.Service.CoResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.DTO.CoResumeListResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;




import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/coresumes")  // (API 요청 처리)
public class CoResumeController {

    private final CoResumeService coResumeService;
    private static final Logger logger = LoggerFactory.getLogger(CoResumeEditController.class);  // 로그 출력을 위한 로거 설정

    @Autowired
    public CoResumeController(CoResumeService coResumeService) {
        this.coResumeService = coResumeService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> saveResume(@RequestBody CoResumeRequestDTO requestDTO) {
        coResumeService.saveResume(requestDTO);

        // ✅ JSON 형식 응답 만들기
        Map<String, String> result = new HashMap<>();
        result.put("message", "이력서 저장 성공");

        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<CoResumeListResponseDTO>> getAllResumes() {
        logger.info("이력서 목록 조회 요청");

        // 이력서 목록을 가져옵니다
        List<CoResumeEntity> resumeEntities = coResumeService.getAllResumes();

        if (resumeEntities.isEmpty()) {
            logger.info("이력서 목록이 비어 있습니다.");
        } else {
            logger.info("불러온 이력서 목록 개수: {}", resumeEntities.size());
        }

        // Entity → DTO 변환
        List<CoResumeListResponseDTO> result = resumeEntities.stream()
                .map(resume -> {
                    String formattedDate = (resume.getCreatedAt() != null)
                            ? new SimpleDateFormat("yyyy-MM-dd").format(resume.getCreatedAt())
                            : "날짜 미제공";  // 날짜가 없으면 "날짜 미제공"으로 표시

                    return new CoResumeListResponseDTO(
                            resume.getId(),
                            resume.getTitle(),
                            formattedDate
                    );
                })
                .collect(Collectors.toList());

        logger.info("이력서 목록 조회 완료, 반환할 데이터 개수: {}", result.size());

        return ResponseEntity.ok(result); // 배열 형태로 반환
    }





    // 이력서 양식 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResume(@PathVariable Long id) {
        coResumeService.deleteResume(id);

        Map<String, String> result = new HashMap<>();
        result.put("message", "이력서 삭제 성공");

        return ResponseEntity.ok(result);
    }


    //수정?
    @GetMapping("/{id}")
    public ResponseEntity<CoResumeRequestDTO> getResumeById(@PathVariable Long id) {
        CoResumeRequestDTO resume = coResumeService.getResumeById(id);
        return ResponseEntity.ok(resume); // 이력서 데이터를 반환
    }

}




