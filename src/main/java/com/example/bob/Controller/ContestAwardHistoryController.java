package com.example.bob.Controller;

import com.example.bob.DTO.ContestAwardHistoryRequestDTO;
import com.example.bob.DTO.ContestAwardHistoryResponseDTO;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

            // ✅ Entity → DTO 변환
            List<ContestAwardHistoryResponseDTO> response = histories.stream()
                    .map(ContestAwardHistoryResponseDTO::fromEntity)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 공모전 내역 불러오기 실패: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> saveAward(@RequestBody ContestAwardHistoryRequestDTO dto,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            ContestAwardHistory history = service.addAwardHistoryForUser(dto, userDetails.getUserEntity());
            return ResponseEntity.ok(ContestAwardHistoryResponseDTO.fromEntity(history));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("저장 실패: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContestHistory(@PathVariable Long id,
                                                  @RequestBody ContestAwardHistoryRequestDTO dto,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Optional<ContestAwardHistory> optional = contestAwardRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.badRequest().body("존재하지 않는 내역입니다.");
            }

            ContestAwardHistory entity = optional.get();
            UserEntity user = userDetails.getUserEntity();

            if (!entity.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }

            // ✅ 업데이트
            entity.setStatus(dto.getStatus());
            entity.setStartDate(service.parseDateOrNull(dto.getStartDate()));
            entity.setEndDate(service.parseDateOrNull(dto.getEndDate()));
            entity.setTitle(dto.getTitle());
            entity.setGrade(dto.getGrade());
            entity.setOrganizer(dto.getOrganizer());

            ContestAwardHistory updated = contestAwardRepository.save(entity);

            return ResponseEntity.ok(ContestAwardHistoryResponseDTO.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("수정 실패: " + e.getMessage());
        }
    }
}
