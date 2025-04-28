package com.example.bob.Controller;

import com.example.bob.Entity.*;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Service.ContestService;
import com.example.bob.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/contest/team")
public class ContestTeamController {

    private final ContestService contestService;
    private final UserRepository userRepository;

    // ✅ 공모전 참가(팀 생성)
    @PostMapping("/create")
    public String createTeam(@RequestParam Long contestId,
                             @RequestParam(required = false) List<String> memberNicknames,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserEntity leader = userDetails.getUserEntity();
        ContestEntity contest = contestService.getById(contestId);

        // 🔹 팀 이름 자동 생성 (김땡땡, 이땡땡, 박땡땡)
        List<String> names = new ArrayList<>();
        names.add(leader.getUserName()); // 팀장 이름 추가

        List<UserEntity> members = new ArrayList<>();

        if (memberNicknames != null) {
            for (String nickname : memberNicknames) {
                userRepository.findByUserNick(nickname).ifPresent(user -> {
                    members.add(user);
                    names.add(user.getUserName());
                });
            }
        }

        String teamName = String.join(", ", names); // 예: 김땡땡, 이땡땡, 박땡땡

        // 🔹 팀 생성
        ContestTeamEntity team = ContestTeamEntity.builder()
                .teamName(teamName)
                .contest(contest)
                .createdBy(leader.getUserNick())
                .status("모집중")
                .createdAt(LocalDate.now())
                .build();

        // 🔹 팀장 저장
        List<ContestTeamMemberEntity> teamMembers = new ArrayList<>();
        teamMembers.add(ContestTeamMemberEntity.builder()
                .team(team)
                .user(leader)
                .role("LEADER")
                .build());

        // 🔹 팀원 저장
        for (UserEntity user : members) {
            teamMembers.add(ContestTeamMemberEntity.builder()
                    .team(team)
                    .user(user)
                    .role("MEMBER")
                    .build());
        }

        // 🔹 팀과 멤버 연결
        team.setMembers(teamMembers);

        // 🔹 저장
        contestService.saveContestTeam(team);

        return "redirect:/contest/" + contestId;
    }
}
