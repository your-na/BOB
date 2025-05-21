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
import com.example.bob.security.CompanyDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@RequestMapping("/api/coresumes")
public class CoResumeController {

    private final CoResumeService coResumeService;
    private static final Logger logger = LoggerFactory.getLogger(CoResumeController.class);

    @Autowired
    public CoResumeController(CoResumeService coResumeService) {
        this.coResumeService = coResumeService;
    }

    // ✅ 이력서 저장 (기업 정보 포함)
    @PostMapping
    public ResponseEntity<Map<String, String>> saveResume(
            @RequestBody CoResumeRequestDTO requestDTO,
            @AuthenticationPrincipal CompanyDetailsImpl companyDetails) {

        // 1️⃣ 기업 ID 가져오기
        Long companyId = companyDetails.getCompanyEntity().getCompanyId();

        // 2️⃣ 이력서 저장 시 companyId 전달
        coResumeService.saveResume(requestDTO, companyId);

        // 3️⃣ 응답 반환
        Map<String, String> result = new HashMap<>();
        result.put("message", "이력서 저장 성공");

        return ResponseEntity.ok(result);
    }


    // ✅ 이력서 목록 조회
    @GetMapping
    public ResponseEntity<List<CoResumeListResponseDTO>> getAllResumes() {
        logger.info("이력서 목록 조회 요청");

        List<CoResumeEntity> resumeEntities = coResumeService.getAllResumes();

        if (resumeEntities.isEmpty()) {
            logger.info("이력서 목록이 비어 있습니다.");
        } else {
            logger.info("불러온 이력서 목록 개수: {}", resumeEntities.size());
        }

        List<CoResumeListResponseDTO> result = resumeEntities.stream()
                .map(resume -> {
                    String formattedDate = (resume.getCreatedAt() != null)
                            ? new SimpleDateFormat("yyyy-MM-dd").format(resume.getCreatedAt())
                            : "날짜 미제공";

                    return new CoResumeListResponseDTO(
                            resume.getId(),
                            resume.getTitle(),
                            formattedDate
                    );
                })
                .collect(Collectors.toList());

        logger.info("이력서 목록 조회 완료, 반환할 데이터 개수: {}", result.size());

        return ResponseEntity.ok(result);
    }

    // ✅ 이력서 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResume(@PathVariable Long id) {
        coResumeService.deleteResume(id);

        Map<String, String> result = new HashMap<>();
        result.put("message", "이력서 삭제 성공");

        return ResponseEntity.ok(result);
    }

    // ✅ 이력서 수정용 조회
    @GetMapping("/{id}")
    public ResponseEntity<CoResumeRequestDTO> getResumeById(@PathVariable Long id) {
        CoResumeRequestDTO resume = coResumeService.getResumeById(id);
        return ResponseEntity.ok(resume);
    }

    // ✅ 이력서 수정
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateResume(@PathVariable Long id, @RequestBody CoResumeRequestDTO updatedResume) {
        coResumeService.updateResume(id, updatedResume);

        Map<String, String> result = new HashMap<>();
        result.put("message", "이력서 수정 성공");

        return ResponseEntity.ok(result);
    }
}
