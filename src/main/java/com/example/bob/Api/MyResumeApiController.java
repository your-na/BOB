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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.example.bob.DTO.MyResumeDto;

import java.io.File;
import java.util.UUID;
import java.util.List;



@Slf4j
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

        log.info("💡 saveResume 요청 - 로그인한 사용자: {}", memberId);
        log.info("💡 saveResume 요청 - 프론트에서 넘어온 memberId: {}", dto.getMemberId());

        dto.setMemberId(memberId);

        Long savedId = myResumeService.save(dto);
        return ResponseEntity.ok(savedId);
    }


    /**
     * 이력서 상세 조회 API
     */
    @GetMapping("/user/resumes/detail/{resumeId}")
    public ResponseEntity<MyResumeDto> getResumeDetail(@PathVariable Long resumeId) {
        MyResumeDto resume = myResumeService.findByIdWithSections(resumeId);
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
                .filter(r -> !r.isDeleted()) // ✅ 삭제되지 않은 이력서만 목록에 표시
                .map(r -> MyResumeDto.builder()
                        .id(r.getId())
                        .title(r.getTitle())
                        .memberId(r.getMemberId())
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

    /**
     * ✅ 나만의 이력서 파일 업로드 API
     */
    @PostMapping("/upload/resumeFiles")
    public ResponseEntity<String> uploadResumeFile(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/resumeFiles/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String uniqueName = UUID.randomUUID() + extension;

            File dest = new File(dir, uniqueName);
            file.transferTo(dest);

            return ResponseEntity.ok(uniqueName); // ← 프론트에서 DB 저장할 파일명
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("파일 업로드 실패: " + e.getMessage());
        }
    }




}
