package com.example.bob.Controller;

import com.example.bob.DTO.MyResumeDto;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.bob.Entity.MyResume;

/**
 * 나만의 이력서 API 컨트롤러
 */
@RestController
@RequestMapping("/api/myresumes")
@RequiredArgsConstructor
public class MyResumeController {

    private final MyResumeService myResumeService;

    /**
     * 나만의 이력서 저장 API
     * - 프론트에서 저장 버튼 클릭 시 호출됨
     */
    @PostMapping
    public ResponseEntity<Long> saveMyResume(@RequestBody MyResumeDto dto) {
        // ✅ 임시 memberId 설정 (userIdLogin 형태)
        dto.setMemberId("testUser"); // 추후 로그인 정보에서 동적으로 설정 예정

        Long savedId = myResumeService.save(dto);
        return ResponseEntity.ok(savedId);
    }

    // ✅ 이력서 상세 JSON 조회 (트리뷰용)
    @GetMapping("/api/user/resumes/detail/{resumeId}")
    public ResponseEntity<MyResume> getResumeDetail(@PathVariable Long resumeId) {
        MyResume resume = myResumeService.findByIdWithSections(resumeId);
        return ResponseEntity.ok(resume);
    }
}
