package com.example.bob.Controller;

import com.example.bob.DTO.ContestRecruitDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.ContestRecruitService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ContestRecruitController {

    private final ContestRecruitService contestRecruitService;

    @PostMapping("/contest/recruit")
    public String createRecruitPost(@ModelAttribute ContestRecruitDTO dto,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity writer = userDetails.getUserEntity();
        contestRecruitService.createRecruitPost(dto, writer);
        return "redirect:/contest/" + dto.getContestId(); // 모집글 작성 후 해당 공모전 상세 페이지로 이동
    }
}
