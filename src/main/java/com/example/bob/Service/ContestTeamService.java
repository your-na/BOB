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
import java.util.Optional;
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
        for (Long userId : dto.getMemberIds()) {
            UserEntity member = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("초대할 사용자를 찾을 수 없습니다: " + userId));

            // ✅ 이미 이 공모전에 참여 중이라면 초대 불가
            boolean alreadyParticipatedInAnyTeam = contestTeamRepository.findByContest(contest).stream()
                    .flatMap(t -> t.getMembers().stream())
                    .anyMatch(m -> m.getUser().getUserId().equals(member.getUserId()));

            if (alreadyParticipatedInAnyTeam) {
                log.info("❌ 이미 공모전 관련 팀에 소속된 사용자: {}", member.getUserNick());
                continue;
            }


            // ✅ 이하 기존 로직 유지
            Optional<ContestTeamMemberEntity> existing = contestTeamMemberRepository.findByTeamAndUser(team, member);

            if (existing.isPresent()) {
                ContestTeamMemberEntity existingMember = existing.get();

                if (existingMember.isInvitePending()) continue;

                if (!existingMember.isAccepted()) {
                    existingMember.setInvitePending(true);
                    existingMember.setAccepted(false);
                    contestTeamMemberRepository.save(existingMember);
                    sendInviteNotification(member, leader, team);
                    continue;
                }

                continue; // 이미 참여 중
            }

            // 새 초대
            ContestTeamMemberEntity newMember = ContestTeamMemberEntity.builder()
                    .team(team)
                    .user(member)
                    .role("MEMBER")
                    .isAccepted(false)
                    .isInvitePending(true)
                    .build();
            contestTeamMemberRepository.save(newMember);
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

        notification.setType(NotificationType.CONTEST_INVITE);

        notificationRepository.save(notification);
    }

    @Transactional
    public void createSoloTeam(Long contestId, UserEntity user) {
        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("공모전 정보를 찾을 수 없습니다."));

        ContestTeamEntity team = ContestTeamEntity.builder()
                .teamName(user.getUserNick() + "의 1인 팀")
                .contest(contest)
                .createdBy(user.getUserNick())
                .status("참가완료")
                .build();
        contestTeamRepository.save(team);

        ContestTeamMemberEntity member = ContestTeamMemberEntity.builder()
                .team(team)
                .user(user)
                .role("LEADER")
                .isAccepted(true)
                .isInvitePending(false)
                .build();
        contestTeamMemberRepository.save(member);
    }

    @Transactional
    public void handleInviteResponse(Long teamId, UserEntity user, boolean accept) {
        ContestTeamMemberEntity member = contestTeamMemberRepository
                .findByTeamIdAndUserId(teamId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 초대를 찾을 수 없습니다."));

        if (!member.isInvitePending()) throw new RuntimeException("이미 응답 처리된 초대입니다.");

        member.setInvitePending(false);
        member.setAccepted(accept);
        contestTeamMemberRepository.save(member);

        notificationRepository.hideByContestTeamIdAndUser(teamId, user);
    }

    // ✅ 내가 팀원으로 참여 중인 공모전 목록 반환
    public List<ContestTeamEntity> getContestsJoinedByUser(UserEntity user) {
        return contestTeamMemberRepository.findByUserAndIsAcceptedTrue(user).stream()
                .filter(m -> !"LEADER".equals(m.getRole()))
                .map(ContestTeamMemberEntity::getTeam)
                .distinct()
                .collect(Collectors.toList());
    }

    public String getNotice(Long contestId) {
        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("공모전 정보를 찾을 수 없습니다."));

        List<ContestTeamEntity> teams = contestTeamRepository.findByContest(contest);

        // 예시: 가장 먼저 생성된 팀의 공지 가져오기
        ContestTeamEntity team = teams.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다."));

        return team.getNotice();
    }

    public void updateNotice(Long contestId, String content, UserEntity requester) {
        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("공모전 정보를 찾을 수 없습니다."));

        List<ContestTeamEntity> teams = contestTeamRepository.findByContest(contest);

        ContestTeamEntity team = teams.stream()
                .filter(t -> t.getMembers().stream()
                        .anyMatch(m -> m.getUser().getUserId().equals(requester.getUserId()) && "LEADER".equals(m.getRole())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("수정 권한이 있는 팀을 찾을 수 없습니다."));

        team.setNotice(content);
        contestTeamRepository.save(team);
    }

    public ContestTeamEntity findTeamByContestAndLeader(ContestEntity contest, UserEntity leader) {
        return contestTeamRepository.findByContest(contest).stream()
                .filter(team -> team.getMembers().stream()
                        .anyMatch(member -> member.getUser().getUserId().equals(leader.getUserId()) && "LEADER".equals(member.getRole())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("팀장을 포함한 팀을 찾을 수 없습니다."));
    }

    public List<ContestTeamEntity> getContestsLedByUser(UserEntity user) {
        return contestTeamRepository.findByCreatedBy(user.getUserNick());
    }



    public Long getInviteId(Long teamId, UserEntity user) {
        return contestTeamMemberRepository.findInviteIdByTeamIdAndUserId(teamId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("초대 정보를 찾을 수 없습니다."));
    }

    public List<String> getAcceptedMemberNicks(ContestTeamEntity team) {
        return contestTeamMemberRepository.findByTeamAndIsAcceptedTrue(team).stream()
                .map(member -> member.getUser().getUserNick())
                .filter(nick -> !nick.equals(team.getCreatedBy()))
                .collect(Collectors.toList());
    }


}
