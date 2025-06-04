package com.example.bob.Controller;

import com.example.bob.DTO.ContestRecruitDTO;
import com.example.bob.Entity.ContestRecruitEntity;
import com.example.bob.Entity.ContestTeamEntity;
import com.example.bob.Entity.NotificationEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.NotificationRepository;
import com.example.bob.Service.ContestRecruitService;
import com.example.bob.Service.ContestService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ContestRecruitController {

    private final ContestRecruitService contestRecruitService;
    private final ContestService contestService;
    private final NotificationRepository notificationRepository;

    @PostMapping("/contest/recruit")
    public String createRecruitPost(@ModelAttribute ContestRecruitDTO dto,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity writer = userDetails.getUserEntity();
        contestRecruitService.createRecruitPost(dto, writer);
        return "redirect:/contest/" + dto.getContestId(); // 모집글 작성 후 해당 공모전 상세 페이지로 이동
    }

    @GetMapping("/recruit/{id}")
    public String getRecruitDetail(@PathVariable Long id,
                                   Model model,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ContestRecruitEntity recruit = contestRecruitService.findById(id);
        UserEntity loginUser = userDetails.getUserEntity();

        boolean isOwner = recruit.getWriter().getUserId().equals(loginUser.getUserId());

        // ✅ 여기만 수정
        boolean alreadyApplied = contestService.hasUserAlreadyApplied(recruit.getId(), loginUser.getUserId());

        model.addAttribute("recruit", recruit);
        model.addAttribute("contest", recruit.getContest().toDTO());
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isAlreadyApplied", alreadyApplied);

        return "recruit_detail";
    }


}
