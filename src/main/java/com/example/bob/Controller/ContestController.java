package com.example.bob.Controller;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Service.ContestService;
import com.example.bob.security.CompanyDetailsImpl;
import com.example.bob.security.CustomUserDetails;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                                @RequestParam("imageUrl") MultipartFile imageUrl,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 사용자 유형 판단
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

        String imageFile = "/images/sample.png";  // 기본값
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                String originalName = imageUrl.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalName;
                Path folderPath = Paths.get("uploads/contestImages");
                Files.createDirectories(folderPath); // 디렉토리 생성

                Path savePath = folderPath.resolve(fileName);
                Files.copy(imageUrl.getInputStream(), savePath);

                imageFile = "/uploads/contestImages/" + fileName; // 웹에서 접근할 경로

                System.out.println("✅ 이미지 저장됨: " + imageFile); // 로그 확인
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ContestEntity contest = ContestEntity.builder()
                .title(dto.getTitle())
                .organizer(dto.getOrganizer())
                .category(dto.getCategory())
                .target(dto.getTarget())
                .region(dto.getRegion())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .judge(dto.getJudge())
                .awardDetails(dto.getAwardDetails())
                .applicationMethod(dto.getApplicationMethod())
                .description(dto.getDescription())
                .imageUrl(imageFile)
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

    @GetMapping("/uploads/contestImages/{fileName}")
    public ResponseEntity<Resource> serveContestImage(@PathVariable String fileName) {
        return contestService.getContestImage(fileName);
    }
}
