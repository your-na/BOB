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

    @PostMapping("/solo")
    public ResponseEntity<?> joinSolo(@RequestBody ContestTeamRequestDTO dto,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            contestTeamService.createSoloTeam(dto.getContestId(), userDetails.getUserEntity());
            return ResponseEntity.ok("혼자 참가 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("혼자 참가 중 오류: " + e.getMessage());
        }
    }

    // 초대 수락 / 거절 API
    @PostMapping("/invite/respond")
    public ResponseEntity<?> respondToInvite(@RequestParam Long teamId,
                                             @RequestParam boolean accept,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            contestTeamService.handleInviteResponse(teamId, userDetails.getUserEntity(), accept);
            return ResponseEntity.ok("응답 처리 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("응답 처리 중 오류: " + e.getMessage());
        }
    }


}
