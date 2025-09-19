package com.example.bob.Controller;

import com.example.bob.DTO.ContestAwardHistoryRequestDTO;
import com.example.bob.Service.ContestHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contest-history")
public class ContestHistoryController {

    private final ContestHistoryService contestHistoryService;

    @PostMapping
    public ResponseEntity<?> addAwardHistory(@RequestBody ContestAwardHistoryRequestDTO req) {
        try {
            contestHistoryService.addAwardHistoryForTeam(req);
            return ResponseEntity.ok("수상 경력이 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("수상 경력 추가 중 오류: " + e.getMessage());
        }
    }
}