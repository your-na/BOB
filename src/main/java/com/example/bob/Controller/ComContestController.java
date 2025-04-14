package com.example.bob.Controller;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Service.ContestService;
import com.example.bob.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ComContestController {

    private final ContestService contestService;

    // ✅ 공모전 주최 신청 화면
    @GetMapping("/comcontest")
    public String comHostPage(Model model) {
        model.addAttribute("contestDTO", new ContestDTO());
        return "comcontest";  // comcontest.html 로 이동
    }

    // ✅ 공모전 주최 신청 처리 (기업 전용)
    @PostMapping("/comcontest")
    public String submitContestRequest(@ModelAttribute ContestDTO contestDTO,
                                       @RequestParam("imageFile") MultipartFile imageFile,
                                       @RequestParam(value = "customCategory", required = false) String customCategory,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 🟡 직접 입력 분야가 있으면 덮어쓰기
        if (customCategory != null && !customCategory.trim().isEmpty()) {
            contestDTO.setCategory(customCategory);
        }

        // 🟡 이미지 저장 처리
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = contestService.saveContestImage(imageFile);
            contestDTO.setImageUrl(imageUrl);
        }

        // 🟡 기타 정보 설정
        contestDTO.setCreatorType("COMPANY");
        contestDTO.setIsApproved(false);
        contestDTO.setIsOnlyBOB(true);

        // 🟡 상태 계산
        String status = contestDTO.getStartDate().isAfter(LocalDate.now()) ? "대기중" : "모집중";
        contestDTO.setStatus(status);

        // 🟡 저장
        ContestEntity contestEntity = contestDTO.toEntity();
        contestService.save(contestEntity);

        return "redirect:/comhome";
    }

    // 관리자
    @GetMapping("/adconlist")
    public String conlistform(Model model) {
        model.addAttribute("contests", contestService.getAllPendingContests()); // 승인되지 않은 공모전만
        return "ad_contest_list";
    }

    @GetMapping("/admin/contest/request/{id}")
    public String viewContestRequest(@PathVariable Long id, Model model) {
        ContestDTO contest = contestService.getContestById(id); // 서비스에서 DTO 반환
        model.addAttribute("contest", contest);
        return "ad_contest_request";
    }

    @PostMapping("/admin/contest/approve")
    public String approveContest(@RequestParam Long id) {
        contestService.approveContest(id);
        return "redirect:/admin/contest/requests";
    }

    @PostMapping("/admin/contest/reject")
    public String rejectContest(@RequestParam Long id) {
        contestService.rejectContest(id); // 필요 시 삭제나 상태 변경
        return "redirect:/admin/contest/requests";
    }
}
