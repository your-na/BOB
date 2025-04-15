package com.example.bob.Controller;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Service.ContestService;
import com.example.bob.security.CompanyDetailsImpl;
import com.example.bob.security.CustomUserDetails;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    // ✅ 사용자 유형에 따라 공모전 홈 리디렉션
    @GetMapping("/contest-redirect")
    public String redirectByUserType(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails instanceof CompanyDetailsImpl) {
            return "redirect:/comhome";
        } else if (userDetails instanceof UserDetailsImpl) {
            return "redirect:/contest";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/adnewcon")
    public String adnewform() {return "ad_newcontest";}

    // ✅ 사용자용 공모전 목록
    @GetMapping("/contest")
    public String contestList(Model model) {
        model.addAttribute("contests", contestService.getApprovedContests());
        return "contest";
    }

    // ✅ 관리자 공모전 목록
    @GetMapping("/ad_contest")
    public String adminContestList(Model model) {
        model.addAttribute("contests", contestService.getApprovedContests());
        return "ad_contest";
    }

    // 기업 공모전 목록
    @GetMapping("/comhome")
    public String comHomePage(Model model) {
        List<ContestDTO> contestList = contestService.getApprovedContests(); // 승인된 공모전만
        model.addAttribute("contestList", contestList);
        return "comhome";
    }

    // ✅ 승인 대기 공모전 목록
    @GetMapping("/ad_contest_list")
    public String pendingList(Model model) {
        model.addAttribute("contests", contestService.getPendingContests());
        return "ad_contest_list";
    }

    // ✅ 공모전 상세 보기 (사용자용)
    @GetMapping("/contest/{id}")
    public String showContestDetail(@PathVariable Long id, Model model) {
        ContestEntity contest = contestService.getById(id);
        model.addAttribute("contest", ContestDTO.fromEntity(contest));
        return "postcontest";
    }

    // 공모전 상세 보기 (기업용)
    @GetMapping("/comhome/{id}")
    public String showCoContestDetail(@PathVariable Long id, Model model) {
        ContestEntity contest = contestService.getById(id);
        model.addAttribute("contest", ContestDTO.fromEntity(contest));
        return "co_postcontest";
    }



    // ✅ 공모전 삭제
    @PostMapping("/admin/contest/delete")
    public String delete(@RequestParam(name = "idsToDelete", required = false) List<Long> idsToDelete) {
        if (idsToDelete != null) {
            idsToDelete.forEach(contestService::deleteById);
        }
        return "redirect:/ad_contest";
    }

    // ✅ 공모전 이미지 서빙
    @GetMapping("/uploads/contestImages/{fileName}")
    public ResponseEntity<Resource> serveContestImage(@PathVariable String fileName) {
        return contestService.getContestImage(fileName);
    }

    // ✅ 공모전 등록 (기업 or 관리자)
    @PostMapping("/contest/create")
    public String createContest(@ModelAttribute ContestDTO dto,
                                @RequestParam("imageUrl") MultipartFile imageUrl,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        String creatorType = "UNKNOWN";
        boolean isOnlyBOB = false;
        boolean isApproved = false;

        if (userDetails instanceof UserDetailsImpl user) {
            creatorType = user.getUserEntity().getRole();
            isApproved = creatorType.equals("ADMIN");
            isOnlyBOB = creatorType.equals("ADMIN");
        } else if (userDetails instanceof CompanyDetailsImpl) {
            creatorType = "COMPANY";
            isApproved = false;
            isOnlyBOB = true;
        } else {
            return "redirect:/login";
        }

        String status = dto.getStartDate().isAfter(LocalDate.now()) ? "대기중" : "모집중";
        String imageFile = "/images/sample.png";

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                String originalName = imageUrl.getOriginalFilename();
                String fileName = System.currentTimeMillis() + "_" + originalName;
                Path folderPath = Paths.get("uploads/contestImages");
                Files.createDirectories(folderPath);
                Path savePath = folderPath.resolve(fileName);
                Files.copy(imageUrl.getInputStream(), savePath);
                imageFile = "/uploads/contestImages/" + fileName;
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

        return isApproved ? "redirect:/ad_contest" : "redirect:/comhome";
    }
}
