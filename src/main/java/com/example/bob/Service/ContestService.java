package com.example.bob.Service;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Entity.ContestHistoryEntity;
import com.example.bob.Repository.ContestHistoryRepository;
import com.example.bob.Repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestHistoryRepository contestHistoryRepository;

    // 공모전 저장
    public ContestEntity save(ContestEntity contest) {
        ContestEntity saved = contestRepository.save(contest);
        saveHistory(saved);
        return saved;
    }

    // 공모전 승인
    public void approve(Long contestId) {
        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공모전을 찾을 수 없습니다."));
        contest.setApproved(true);
        contestRepository.save(contest);
        saveHistory(contest);
    }

    // 공모전 히스토리 저장
    private void saveHistory(ContestEntity contest) {
        ContestHistoryEntity history = ContestHistoryEntity.fromEntity(contest);
        contestHistoryRepository.save(history);
    }

    // 전체 공모전 리스트 (관리자)
    public List<ContestDTO> getAllContests() {
        return contestRepository.findAll().stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 사용자에게 보여줄 승인된 공모전 리스트
    public List<ContestDTO> getApprovedContests() {
        return contestRepository.findByIsApprovedTrue().stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 승인 대기 공모전
    public List<ContestDTO> getPendingContests() {
        return contestRepository.findByIsApprovedFalse().stream()
                .map(ContestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ContestEntity getById(Long id) {
        return contestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 공모전 없음"));
    }
}
