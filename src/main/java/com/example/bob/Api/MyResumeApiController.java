package com.example.bob.Api;

import com.example.bob.DTO.MyResumeDto;
import com.example.bob.Entity.MyResume;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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

    /**
     * ✅ [1단계] 로그인한 사용자의 이력서 목록 조회 API
     * - 사용자 본인이 작성한 모든 이력서(title, id 등)를 JSON으로 반환
     * - 프론트에서 '나의 이력서' 버튼 목록으로 사용
     */
    @GetMapping
    public ResponseEntity<List<MyResumeDto>> getMyResumes() {
        // 🔐 현재 로그인된 사용자의 ID 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberId = auth.getName(); // 로그인된 사용자의 ID (user_id_login)

        // 🧾 이 사용자의 이력서 목록 조회
        List<MyResume> resumes = myResumeService.findAllByMemberId(memberId);

        // 📦 최소 정보만 담은 DTO로 변환
        List<MyResumeDto> dtos = resumes.stream()
                .map(r -> MyResumeDto.builder()
                        .title(r.getTitle())       // 이력서 제목
                        .memberId(r.getMemberId()) // 사용자 ID
                        .build()
                )
                .toList();

        // 📤 JSON으로 응답
        return ResponseEntity.ok(dtos);
    }

    /**
     * ✅ [3단계] 이력서 삭제 API
     * - 로그인한 사용자만 자신의 이력서를 삭제 가능
     */
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteMyResume(@PathVariable Long resumeId) {
        // 🔐 현재 로그인 사용자 ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberId = auth.getName();

        // 🗑️ 삭제 요청
        myResumeService.deleteResume(resumeId, memberId);

        return ResponseEntity.noContent().build(); // 상태 204 반환
    }


}
