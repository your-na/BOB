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
}
