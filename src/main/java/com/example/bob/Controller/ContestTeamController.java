package com.example.bob.Controller;

import com.example.bob.DTO.ContestTeamRequestDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.ContestTeamService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contest/team")
public class ContestTeamController {

    private final ContestTeamService contestTeamService;

    @PostMapping("/create")
    public ResponseEntity<?> createContestTeam(@RequestBody ContestTeamRequestDTO dto,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        UserEntity leader = userDetails.getUserEntity();

        try {
            contestTeamService.createTeamAndSendInvites(dto, leader);
            return ResponseEntity.ok("팀 생성 및 초대 전송 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("팀 생성 중 오류: " + e.getMessage());
        }
    }
}
