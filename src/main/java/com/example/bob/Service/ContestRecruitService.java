package com.example.bob.Service;

import com.example.bob.DTO.ContestRecruitDTO;
import com.example.bob.Entity.*;
import com.example.bob.Repository.ContestRecruitRepository;
import com.example.bob.Repository.ContestRepository;
import com.example.bob.Repository.ContestTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContestRecruitService {

    private final ContestRecruitRepository contestRecruitRepository;
    private final ContestRepository contestRepository;
    private final ContestTeamRepository contestTeamRepository;

    @Transactional
    public void createRecruitPost(ContestRecruitDTO dto, UserEntity writer) {
        ContestEntity contest = contestRepository.findById(dto.getContestId())
                .orElseThrow(() -> new IllegalArgumentException("공모전 정보를 찾을 수 없습니다."));

        ContestRecruitEntity recruit = ContestRecruitEntity.builder()
                .title(dto.getTitle())
                .description(dto.getContent())
                .recruitmentStartDate(dto.getRecruitStartDate())
                .recruitmentEndDate(dto.getRecruitEndDate())
                .startDate(dto.getProjectStartDate())
                .endDate(dto.getProjectEndDate())
                .recruitCount(dto.getRecruitCountAsInt())
                .writer(writer)
                .contest(contest)
                .build();

        ContestTeamEntity team = ContestTeamEntity.builder()
                .teamName(dto.getTitle() + " 팀")
                .contest(contest)
                .createdBy(writer.getUserNick())
                .status("모집중")
                .build();

        ContestTeamMemberEntity leader = ContestTeamMemberEntity.builder()
                .team(team)
                .user(writer)
                .isAccepted(true)
                .isInvitePending(false)
                .role("LEADER")
                .build();

        team.getMembers().add(leader);

        contestTeamRepository.save(team);
        contestRecruitRepository.save(recruit);
    }

    // ContestRecruitService.java
    @Transactional(readOnly = true)
    public List<ContestRecruitEntity> findByContestId(Long contestId) {
        return contestRecruitRepository.findByContest_Id(contestId);
    }

}
