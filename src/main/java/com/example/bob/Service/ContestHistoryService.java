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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContestHistoryService {

    private final ContestAwardRepository contestAwardRepository;
    private final ContestTeamRepository contestTeamRepository;

    @Transactional
    public void addAwardHistoryForTeam(ContestAwardHistoryRequestDTO req) {
        // 1) 팀 조회
        ContestTeamEntity team = contestTeamRepository.findById(req.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        // 2) ContestEntity (공모전 정보)
        String title = team.getContest().getTitle();
        LocalDate startDate = team.getContest().getStartDate();
        LocalDate endDate = team.getContest().getEndDate();

        // 3) 팀 멤버(UserEntity) 조회
        List<UserEntity> members = team.getMembers().stream()
                .map(ContestTeamMemberEntity::getUser)
                .toList();

        // 4) 모든 멤버에게 수상 경력 기록 추가
        for (UserEntity member : members) {
            ContestAwardHistory history = ContestAwardHistory.builder()
                    .user(member)
                    .grade(req.getGrade())
                    .organizer(req.getOrganizer())
                    .source(req.getSource())
                    .title(title)
                    .status("참여완료")
                    .startDate(startDate)
                    .endDate(endDate)
                    .ocrRawText(req.getOcrRawText()) // ✅ OCR 원문 저장
                    .teamId(req.getTeamId())         // ✅ 팀 아이디 저장
                    .createdAt(java.time.LocalDateTime.now()) // ✅ 생성시각 저장
                    .build();

            contestAwardRepository.save(history);
        }
    }

    // OCR 결과 → 수상, 기관 추출
    public String extractText(MultipartFile file) {
        // Tesseract OCR 처리 로직 (간단히 흉내만)
        return "OCR_RAW_TEXT_SAMPLE";
    }
    public String extractGrade(String text) {
        // 정규식 파싱
        return text.contains("대상") ? "대상" : "기타";
    }
    public String extractOrganizer(String text) {
        return "주최기관";
    }

    // 실제 저장
    @Transactional
    public void addAwardHistoryForUser(ContestAwardHistoryRequestDTO req, UserEntity user) {
        ContestAwardHistory history = ContestAwardHistory.builder()
                .user(user)
                .title(req.getTitle())
                .grade(req.getGrade())
                .organizer(req.getOrganizer())
                .ocrRawText(req.getOcrRawText())
                .status("참여완료")
                .source(req.getSource())
                .build();
        contestAwardRepository.save(history);
    }
}
