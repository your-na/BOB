package com.example.bob.Service;

import com.example.bob.DTO.ContestRecruitDTO;
import com.example.bob.Entity.*;
import com.example.bob.Repository.ContestRecruitRepository;
import com.example.bob.Repository.ContestRepository;
import com.example.bob.Repository.ContestTeamRepository;
import com.example.bob.Repository.ContestTeamMemberRepository;
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
    private final ContestTeamMemberRepository contestTeamMemberRepository;

    @Transactional
    public void createRecruitPost(ContestRecruitDTO dto, UserEntity writer) {
        ContestEntity contest = contestRepository.findById(dto.getContestId())
                .orElseThrow(() -> new IllegalArgumentException("공모전 정보를 찾을 수 없습니다."));

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
                .team(team)
                .build();

        contestTeamRepository.save(team);
        contestRecruitRepository.save(recruit);
    }

    // ContestRecruitService.java
    @Transactional(readOnly = true)
    public List<ContestRecruitEntity> findByContestId(Long contestId) {
        return contestRecruitRepository.findByContest_Id(contestId);
    }

    @Transactional(readOnly = true)
    public ContestRecruitEntity findById(Long id) {
        return contestRecruitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));
    }

    @Transactional
    public void acceptApplication(Long applicationId) {
        ContestTeamMemberEntity member = contestTeamMemberRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));

        member.setAccepted(true);
        member.setInvitePending(false); // 수락 시 대기 상태 종료
        contestTeamMemberRepository.save(member);
    }

    @Transactional
    public void rejectApplication(Long applicationId) {
        if (!contestTeamMemberRepository.existsById(applicationId)) {
            throw new IllegalArgumentException("해당 신청이 존재하지 않습니다.");
        }
        contestTeamMemberRepository.deleteById(applicationId);
    }
}
