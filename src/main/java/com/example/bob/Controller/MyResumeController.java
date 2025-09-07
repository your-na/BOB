package com.example.bob.Controller;

import com.example.bob.DTO.MyResumeDto;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        // 임시 memberId 설정 (로그인 연동 전)
        dto.setMemberId(1L); // 나중에 로그인 정보에서 가져오게 수정 가능

        Long savedId = myResumeService.save(dto);
        return ResponseEntity.ok(savedId);
    }
}
