package com.example.bob.Service;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Entity.ContestHistoryEntity;
import com.example.bob.Repository.ContestHistoryRepository;
import com.example.bob.Repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
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



}
