package com.example.bob.Controller;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Service.ContestService;
import com.example.bob.security.CompanyDetailsImpl;
import com.example.bob.security.CustomUserDetails;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    // 사용자 공모전 페이지
    @GetMapping("/contest")
    public String contestList(Model model) {
        List<ContestDTO> contests = contestService.getApprovedContests();

        model.addAttribute("contests", contests);
        return "contest"; // contest.html
    }

    // 관리자 페이지 - 공모전 목록 + 승인 대기
    @GetMapping("/ad_contest")
    public String adminContestList(Model model) {
        model.addAttribute("pending", contestService.getPendingContests());
        model.addAttribute("all", contestService.getAllContests());
        return "ad_contest";
    }

    // 공모전 등록 처리 (관리자 or 기업)
    @PostMapping("/contest/create")
    public String createContest(@ModelAttribute ContestDTO dto,
                                @RequestParam("imageFile") MultipartFile imageFile,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        // ✅ 사용자 유형 판단
        String creatorType = "UNKNOWN";
        boolean isOnlyBOB = false;
        boolean isApproved = false;

        if (userDetails instanceof UserDetailsImpl user) {
            creatorType = user.getUserEntity().getRole();  // ADMIN 또는 USER
            isApproved = creatorType.equals("ADMIN");
            isOnlyBOB = creatorType.equals("ADMIN");
        } else if (userDetails instanceof CompanyDetailsImpl company) {
            creatorType = "COMPANY";
            isApproved = false; // 기업은 승인이 필요
            isOnlyBOB = true;   // 기업 공모전은 always ONLY BOB
        } else {
            return "redirect:/login"; // 인증되지 않은 사용자 처리
        }

        String status = dto.getStartDate().isAfter(LocalDate.now()) ? "대기중" : "모집중";

        // ✅ 이미지 저장
        String imageUrl = "/images/sample.png";

        ContestEntity contest = ContestEntity.builder()
                .title(dto.getTitle())
                .organizer(dto.getOrganizer())
                .category(dto.getCategory())
                .target(dto.getTarget())
                .region(dto.getRegion())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .judgeStartDate(dto.getJudgeStartDate())
                .judgeEndDate(dto.getJudgeEndDate())
                .awardDetails(dto.getAwardDetails())
                .applicationMethod(dto.getApplicationMethod())
                .description(dto.getDescription())
                .imageUrl(imageUrl)
                .status(status)
                .creatorType(creatorType)
                .isOnlyBOB(isOnlyBOB)
                .isApproved(isApproved)
                .build();

        contestService.save(contest);
        return "redirect:/contest";
    }


    // 공모전 승인
    @PostMapping("/admin/contest/approve/{id}")
    public String approveContest(@PathVariable Long id) {
        contestService.approve(id);
        return "redirect:/ad_contest";
    }
}
