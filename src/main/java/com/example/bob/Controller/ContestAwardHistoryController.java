package com.example.bob.Controller;

import com.example.bob.Entity.ContestAwardHistory;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.ContestAwardRepository;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ContestAwardHistoryController {

    private final ContestAwardRepository contestAwardRepository;

    // ✅ 공모전 내역 페이지 (Thymeleaf 렌더링)
    @GetMapping("/contesthistory")
    public String showContestHistoryPage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        Long userId = userDetails.getUserEntity().getUserId();

        List<ContestAwardHistory> submittedContests =
                contestAwardRepository.findByUser_UserId(userId);

        model.addAttribute("submittedContests", submittedContests);

        return "history"; // templates/history.html
    }

    // ✅ 공모전 내역 API (JS fetch용)
    @GetMapping("/api/contest-history")
    @ResponseBody
    public ResponseEntity<?> getContestHistories(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            UserEntity user = userDetails.getUserEntity();
            List<ContestAwardHistory> histories = contestAwardRepository.findByUser(user);

            return ResponseEntity.ok(histories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 공모전 내역 불러오기 실패: " + e.getMessage());
        }
    }

    // ✅ 공모전 내역 삭제 API
    @DeleteMapping("/api/contest-history/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteContestHistory(@PathVariable Long id) {
        try {
            contestAwardRepository.deleteById(id);
            return ResponseEntity.ok("삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }
}
