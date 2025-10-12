package com.example.bob.Service;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.*;
import com.example.bob.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;


import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestHistoryRepository contestHistoryRepository;
    private final ContestTeamRepository contestTeamRepository;
    private final ContestTeamMemberRepository contestTeamMemberRepository;
    private final ContestRecruitRepository contestRecruitRepository;

    @Autowired
    private final NotificationRepository notificationRepository;

    // 공모전 저장
    public ContestEntity save(ContestEntity contest) {
        ContestEntity saved = contestRepository.save(contest);
        saveHistory(saved);
        return saved;
    }

    public void reject(Long id) {
        ContestEntity c = contestRepository.findById(id).orElseThrow();
        c.setApproved(false);
        contestRepository.save(c);
        saveHistory(c);
    }

    // 공모전 히스토리 저장
    private void saveHistory(ContestEntity contest) {
        ContestHistoryEntity history = ContestHistoryEntity.fromEntity(contest);
        contestHistoryRepository.save(history);
    }

    // 전체 공모전 리스트 (관리자)
    public List<ContestDTO> getAllContests() {
        return contestRepository.findByIsDeletedFalse().stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 사용자에게 보여줄 승인된 공모전 리스트
    public List<ContestDTO> getApprovedContests() {
        return contestRepository.findByIsApprovedTrueAndIsDeletedFalse().stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 승인 대기 공모전
    public List<ContestDTO> getPendingContests() {
        return contestRepository.findByIsApprovedFalse().stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ContestDTO> getAllPendingContests() {
        return contestRepository.findByIsApprovedFalseAndIsDeletedFalse()  // 🔥 조건 추가 필요
                .stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void approveContest(Long id) {
        ContestEntity contest = contestRepository.findById(id).orElseThrow();
        contest.setApproved(true);
        contestRepository.save(contest);
    }

    public void rejectContest(Long id) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공모전 없음"));
        contest.setDeleted(true); // 실제 삭제하지 않고 숨김 처리
        contestRepository.save(contest);
        saveHistory(contest);     // 변경 내용 히스토리에도 저장
    }

    public ContestEntity getById(Long id) {
        return contestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 공모전 없음"));
    }

    public ContestDTO getContestById(Long id) {
        return contestRepository.findById(id)
                .map(ContestDTO::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("공모전을 찾을 수 없습니다."));
    }

    private final String uploadDir = "uploads/contestImages/";

    public String saveContestImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path savePath = Paths.get(uploadDir + fileName);
            Files.createDirectories(savePath.getParent());
            Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/contestImages/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("공모전 이미지를 저장할 수 없습니다.", e);
        }
    }

    public ResponseEntity<Resource> getContestImage(String fileName) {
        try {
            String decodedName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
            Path path = Paths.get(uploadDir + decodedName);

            if (Files.exists(path)) {
                Resource file = new UrlResource(path.toUri());
                return ResponseEntity.ok().body(file);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 공모전 삭제
    public void deleteById(Long id) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공모전 없음"));
        contest.setDeleted(true);
        contestRepository.save(contest);

        // 삭제도 이력에 남김
        saveHistory(contest);
    }

    // 필터
    public List<ContestDTO> filterContests(String organizer, String region) {
        return contestRepository.findAll().stream()
                .filter(c -> (organizer == null || c.getOrganizer().contains(organizer)))
                .filter(c -> (region == null || c.getRegion().contains(region)))
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }


    public List<ContestDTO> getContestsByCreatorType(String creatorType) {
        return contestRepository.findByCreatorTypeAndIsDeletedFalse(creatorType).stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }


    public void saveContestTeam(ContestTeamEntity team) {
        contestTeamRepository.save(team);
    }

    // ✅ 공모전 팀 홈 데이터 조회
    public Map<String, Object> getContestTeamHomeData(Long teamId, UserEntity loginUser) {
        ContestTeamEntity team = contestTeamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("공모전 팀을 찾을 수 없습니다."));

        ContestEntity contest = team.getContest();
        String ownerNick = team.getCreatedBy();
        String loginNick = loginUser.getUserNick();

        List<String> teamMembers = team.getMembers().stream()
                .map(m -> m.getUser().getUserNick())
                .filter(nick -> !nick.equals(ownerNick))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("project", contest);         // 공모전 기본 정보 (제목 등 출력용)
        result.put("team", team);               // 공모전 팀 정보 (공지사항, 팀명 등)
        result.put("ownerNick", ownerNick);     // 팀장 닉네임
        result.put("loginNick", loginNick);     // 현재 사용자 닉네임
        result.put("teamMembers", teamMembers); // 팀원 목록

        return result;
    }

    // ✅ 공모전 팀 공지사항 수정
    @Transactional
    public void updateTeamNotice(Long teamId, String content, UserEntity loginUser) {
        ContestTeamEntity team = contestTeamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("공모전 팀을 찾을 수 없습니다."));

        if (!team.getCreatedBy().equals(loginUser.getUserNick())) {
            throw new AccessDeniedException("공지 수정 권한이 없습니다.");
        }

        team.setNotice(content);
        contestTeamRepository.save(team);
    }

    public List<ContestDTO> getLatestContests() {
        List<ContestEntity> latest = contestRepository.findTop4ByIsApprovedTrueOrderByCreatedAtDesc();
        return latest.stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendApplicationNotification(Long recruitId, UserEntity applicant) {
        ContestRecruitEntity recruit = contestRecruitRepository.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

        ContestEntity contest = recruit.getContest();
        UserEntity owner = recruit.getWriter();

        if (hasUserAlreadyApplied(recruitId, applicant.getUserId())) {
            return; // 이미 신청했다면 무시
        }

        ContestTeamEntity team = recruit.getTeam();
        if (team == null) {
            throw new IllegalArgumentException("모집글에 연결된 팀이 없습니다.");
        }

        ContestTeamMemberEntity member = ContestTeamMemberEntity.builder()
                .team(team)
                .user(applicant)
                .isAccepted(false)       // 수락 X
                .isInvitePending(true)   // 신청 상태
                .role("MEMBER")
                .build();
        contestTeamMemberRepository.save(member);

        // ✅ 알림 저장
        String message = applicant.getUserNick() + "님이 \"" + contest.getTitle() + "\" 팀 모집글에 참가 신청을 보냈습니다.";

        NotificationEntity notification = new NotificationEntity();
        notification.setUser(owner);
        notification.setSender(applicant);
        notification.setMessage(message);
        notification.setRelatedRecruit(recruit);
        notification.setType(NotificationType.CONTEST_APPLICATION);
        notification.setRelatedContest(contest);
        notification.setTimestamp(LocalDateTime.now());
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }


    public boolean hasUserAlreadyApplied(Long recruitId, Long userId) {
        // 모집글을 가져온다
        ContestRecruitEntity recruit = contestRecruitRepository.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("모집글 없음"));

        ContestEntity contest = recruit.getContest();
        UserEntity user = new UserEntity();
        user.setUserId(userId);

        // 알림 기준: 이미 신청 알림을 보냈는지?
        boolean alreadyNotified = notificationRepository.existsByUserAndTypeAndRelatedContestAndSender(
                recruit.getWriter(),
                NotificationType.CONTEST_APPLICATION,
                contest,
                user
        );

        // 팀 멤버 기준: 아직 초대 상태인지?
        List<ContestTeamMemberEntity> memberList = contestTeamMemberRepository
                .findByUserAndTeam_Contest(user, contest);
        boolean stillPending = memberList.stream()
                .anyMatch(m -> m.isInvitePending() && !m.isAccepted());

        return alreadyNotified || stillPending;
    }

    //웹페이지 내 전체 검색 관련
    // ✅ 공모전 검색 기능
    public List<ContestEntity> search(String keyword) {
        return contestRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(keyword);
    }

}
