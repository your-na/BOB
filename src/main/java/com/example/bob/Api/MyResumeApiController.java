package com.example.bob.Api;

import com.example.bob.DTO.MyResumeDto;
import com.example.bob.Entity.MyResume;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@RestController
@RequestMapping("/api/myresumes")
@RequiredArgsConstructor
public class MyResumeApiController {

    private final MyResumeService myResumeService;

    /**
     * 이력서 저장 API
     */
    @PostMapping
    public ResponseEntity<Long> saveMyResume(@RequestBody MyResumeDto dto) {
        // 🔐 현재 로그인 사용자 ID 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberId = auth.getName(); // 로그인한 사용자 ID
        dto.setMemberId(memberId);

        Long savedId = myResumeService.save(dto);
        return ResponseEntity.ok(savedId);
    }


    /**
     * 이력서 상세 조회 API
     */
    @GetMapping("/user/resumes/detail/{resumeId}")
    public ResponseEntity<MyResume> getResumeDetail(@PathVariable Long resumeId) {
        MyResume resume = myResumeService.findByIdWithSections(resumeId);
        return ResponseEntity.ok(resume);
    }
}
