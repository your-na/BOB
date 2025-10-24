package com.example.bob.Service;

import com.example.bob.DTO.ContestAwardHistoryRequestDTO;
import com.example.bob.Entity.ContestAwardHistory;
import com.example.bob.Entity.ContestTeamEntity;
import com.example.bob.Entity.ContestTeamMemberEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.ContestAwardRepository;
import com.example.bob.Repository.ContestTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContestHistoryService {

    private final ContestAwardRepository contestAwardRepository;
    private final ContestTeamRepository contestTeamRepository;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ✅ 팀 단위 수상 기록 저장 (모든 멤버에게 추가)
    @Transactional
    public List<ContestAwardHistory> addAwardHistoryForTeam(ContestAwardHistoryRequestDTO req) {
        ContestTeamEntity team = contestTeamRepository.findById(req.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        // 시작일: 팀 생성일, 종료일: 제출한 오늘 날짜
        LocalDate startDate = team.getCreatedAt();
        LocalDate endDate = LocalDate.now();

        List<UserEntity> members = team.getMembers().stream()
                .map(ContestTeamMemberEntity::getUser)
                .toList();

        return members.stream().map(member -> {
            ContestAwardHistory history = ContestAwardHistory.builder()
                    .user(member)
                    .grade(req.getGrade())
                    .organizer(req.getOrganizer())
                    .source(req.getSource())
                    .title(req.getTitle() != null ? req.getTitle() : team.getContest().getTitle())
                    .status(req.getStatus() != null ? req.getStatus() : "참여완료")
                    .startDate(startDate)
                    .endDate(endDate)
                    .ocrRawText(req.getOcrRawText())
                    .teamId(req.getTeamId())
                    .build();
            return contestAwardRepository.save(history);
        }).toList();
    }

    // ✅ 개인 수상 기록 저장
    @Transactional
    public ContestAwardHistory addAwardHistoryForUser(ContestAwardHistoryRequestDTO req, UserEntity user) {
        ContestAwardHistory history = ContestAwardHistory.builder()
                .user(user)
                .title(req.getTitle())
                .grade(req.getGrade())
                .organizer(req.getOrganizer())
                .ocrRawText(req.getOcrRawText())
                .status(req.getStatus() != null ? req.getStatus() : "참여완료")
                .source(req.getSource())
                .startDate(parseDateOrNull(req.getStartDate()))
                .endDate(parseDateOrNull(req.getEndDate()))
                .build();
        return contestAwardRepository.save(history);
    }

    // ✅ 공통 날짜 변환 유틸
    public LocalDate parseDateOrNull(String dateStr) {
        try {
            return (dateStr != null && !dateStr.isBlank())
                    ? LocalDate.parse(dateStr, fmt)
                    : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ✅ 로그인한 사용자의 공모전 이력 전체 조회
    @Transactional(readOnly = true)
    public List<ContestAwardHistory> getContestAwardsByUser(UserEntity user) {
        return contestAwardRepository.findByUser(user);
    }

    // ✅ 공모전 단건 조회 (상세 보기용, 필요 시)
    @Transactional(readOnly = true)
    public ContestAwardHistory getContestAwardById(Long id, UserEntity user) {
        ContestAwardHistory award = contestAwardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공모전 이력이 없습니다."));
        if (!award.getUser().equals(user)) {
            throw new IllegalArgumentException("본인 이력만 조회할 수 있습니다.");
        }
        return award;
    }


    // ✅ OCR 관련 (임시)
    public String extractText(MultipartFile file) {
        return "OCR_RAW_TEXT_SAMPLE";
    }
    public String extractGrade(String text) {
        return text != null && text.contains("대상") ? "대상" : "";
    }
    public String extractOrganizer(String text) {
        return "주최기관";
    }
}
