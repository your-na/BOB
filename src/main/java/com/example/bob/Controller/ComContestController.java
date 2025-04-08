package com.example.bob.Controller;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Service.ContestService;
import com.example.bob.security.CompanyDetailsImpl;
import com.example.bob.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ComContestController {

    private final ContestService contestService;

    // ✅ 공모전 주최 신청 화면
    @GetMapping("/comhost")
    public String comHostPage() {
        return "comhost";  // comhost.html 로 이동
    }

    // ✅ 공모전 주최 신청 처리 (기업 전용)
    @PostMapping("/comhost")
    public String submitContestRequest(@ModelAttribute ContestDTO contestDTO,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 기업 정보 추가 반영
        contestDTO.setCreatorType("COMPANY");
        contestDTO.setIsApproved(false);
        contestDTO.setIsOnlyBOB(true);

        // 모집 상태 계산
        String status = contestDTO.getStartDate().isAfter(LocalDate.now()) ? "대기중" : "모집중";
        contestDTO.setStatus(status);

        // 이미지 없이 저장하는 기본 처리 (추후 이미지 업로드 확장 가능)
        ContestEntity contestEntity = contestDTO.toEntity();
        contestService.save(contestEntity);

        return "redirect:/ad_contest_list";  // 관리자 목록 페이지로 이동
    }
}
