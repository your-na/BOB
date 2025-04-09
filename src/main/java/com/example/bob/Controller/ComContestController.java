package com.example.bob.Controller;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Service.ContestService;
import com.example.bob.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ComContestController {

    private final ContestService contestService;

    // ✅ 공모전 주최 신청 화면
    @GetMapping("/comhost")
    public String comHostPage(Model model) {
        model.addAttribute("contestDTO", new ContestDTO());
        return "comhost";  // comhost.html 로 이동
    }

    // ✅ 공모전 주최 신청 처리 (기업 전용)
    @PostMapping("/comhost")
    public String submitContestRequest(@ModelAttribute ContestDTO contestDTO,
                                       @RequestParam("posterImage") MultipartFile posterImage,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 기업 정보 반영
        contestDTO.setCreatorType("COMPANY");
        contestDTO.setIsApproved(false);
        contestDTO.setIsOnlyBOB(true);

        // 모집 상태 계산
        String status = contestDTO.getStartDate().isAfter(LocalDate.now()) ? "대기중" : "모집중";
        contestDTO.setStatus(status);

        // ✅ 이미지 업로드
        if (!posterImage.isEmpty()) {
            String imageUrl = contestService.saveContestImage(posterImage);
            contestDTO.setImageUrl(imageUrl);
        }

        // 저장
        ContestEntity contestEntity = contestDTO.toEntity();
        contestService.save(contestEntity);

        return "redirect:/comhome"; // 기업 홈으로 이동
    }
}
