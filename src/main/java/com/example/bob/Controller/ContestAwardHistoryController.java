package com.example.bob.Controller;

import com.example.bob.DTO.ContestAwardHistoryRequestDTO;
import com.example.bob.Entity.ContestAwardHistory;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.ContestAwardRepository;
import com.example.bob.Service.ContestHistoryService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contest-history")
public class ContestAwardHistoryController {

    private final ContestAwardRepository contestAwardRepository;
    private final ContestHistoryService service;

    // ✅ 로그인한 사용자의 공모전 내역 조회
    @GetMapping
    public ResponseEntity<?> getContestHistories(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            UserEntity user = userDetails.getUserEntity();
            List<ContestAwardHistory> histories = contestAwardRepository.findByUser(user);
            return ResponseEntity.ok(histories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 공모전 내역 불러오기 실패: " + e.getMessage());
        }
    }

    // ✅ 공모전 내역 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContestHistory(@PathVariable Long id) {
        try {
            contestAwardRepository.deleteById(id);
            return ResponseEntity.ok("삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

    // ✅ OCR 업로드 (수상명 / 주최기관 추출)
    @PostMapping("/ocr")
    public ResponseEntity<Map<String, String>> extractOcr(@RequestParam("file") MultipartFile file) {
        try {
            String ocrRawText = service.extractText(file);
            String grade = service.extractGrade(ocrRawText);
            String organizer = service.extractOrganizer(ocrRawText);

            return ResponseEntity.ok(Map.of(
                    "grade", grade,
                    "organizer", organizer,
                    "ocrRawText", ocrRawText
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "OCR 실패: " + e.getMessage()));
        }
    }

    // ✅ OCR → 사용자 입력(title) → 최종 저장 (개인)
    @PostMapping
    public ResponseEntity<String> saveAward(@RequestBody ContestAwardHistoryRequestDTO dto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            service.addAwardHistoryForUser(dto, userDetails.getUserEntity());
            return ResponseEntity.ok("저장 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("저장 실패: " + e.getMessage());
        }
    }

    // ✅ 팀 단위 수상 기록 추가
    @PostMapping("/team")
    public ResponseEntity<?> addAwardHistoryForTeam(@RequestBody ContestAwardHistoryRequestDTO req) {
        try {
            service.addAwardHistoryForTeam(req);
            return ResponseEntity.ok("팀 수상 경력이 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("팀 수상 경력 추가 중 오류: " + e.getMessage());
        }
    }
}
