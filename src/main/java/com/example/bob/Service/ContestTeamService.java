package com.example.bob.Service;

import com.example.bob.DTO.ContestTeamRequestDTO;
import com.example.bob.Entity.*;
import com.example.bob.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestTeamService {

    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final ContestTeamRepository contestTeamRepository;
    private final ContestTeamMemberRepository contestTeamMemberRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createTeamAndSendInvites(ContestTeamRequestDTO dto, UserEntity leader) {
        // 1. 팀 생성
        ContestEntity contest = contestRepository.findById(dto.getContestId())
                .orElseThrow(() -> new RuntimeException("공모전 정보를 찾을 수 없습니다."));

        ContestTeamEntity team = ContestTeamEntity.builder()
                .teamName(leader.getUserNick() + "의 팀")
                .contest(contest)
                .createdBy(leader.getUserNick())
                .status("모집중")
                .build();

        contestTeamRepository.save(team);

        // 2. 팀장 등록 (바로 accepted 처리)
        ContestTeamMemberEntity leaderMember = ContestTeamMemberEntity.builder()
                .team(team)
                .user(leader)
                .role("LEADER")
                .isAccepted(true)
                .isInvitePending(false)
                .build();
        contestTeamMemberRepository.save(leaderMember);

        // 3. 초대할 팀원들 처리
        for (String userId : dto.getMemberIds()) {
            UserEntity member = userRepository.findByUserIdLogin(userId)
                    .orElseThrow(() -> new RuntimeException("초대할 사용자를 찾을 수 없습니다: " + userId));

            ContestTeamMemberEntity memberEntity = ContestTeamMemberEntity.builder()
                    .team(team)
                    .user(member)
                    .role("MEMBER")
                    .isAccepted(false)
                    .isInvitePending(true)
                    .build();
            contestTeamMemberRepository.save(memberEntity);

            // ✅ 알림 전송
            sendInviteNotification(member, leader, team);
        }
    }


    private void sendInviteNotification(UserEntity receiver, UserEntity sender, ContestTeamEntity team) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(receiver); // 알림 받는 사람
        notification.setSender(sender); // 초대한 사람 (팀장)
        notification.setContestTeam(team); // 관련 팀
        notification.setMessage(sender.getUserNick() + " 님이 '" + team.getTeamName() + "' 팀에 초대했습니다.");
        notification.setIsRead(false);
        notification.setTimestamp(LocalDateTime.now());

        notificationRepository.save(notification);
    }

}
