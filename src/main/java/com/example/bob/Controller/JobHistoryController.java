package com.example.bob.Controller;

import com.example.bob.DTO.JobHistoryDTO;
import com.example.bob.Entity.JobHistoryEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.JobHistoryRepository;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job-history")
public class JobHistoryController {

    private final JobHistoryRepository jobHistoryRepository;

    // ✅ 내 구직내역 전체 조회
    @GetMapping
    public List<JobHistoryDTO> getMyJobHistory(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserEntity().getUserId();
        List<JobHistoryEntity> histories = jobHistoryRepository.findByUserEntity_UserIdOrderByStartDateDesc(userId);
        return histories.stream().map(JobHistoryEntity::toDTO).collect(Collectors.toList());
    }

    // ✅ 새 구직내역 추가
    @PostMapping
    public ResponseEntity<JobHistoryDTO> addJobHistory(@RequestBody JobHistoryDTO dto,
                                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();
        System.out.println("현재 로그인 사용자: " + user.getUserId());
        JobHistoryEntity entity = JobHistoryEntity.fromDTO(dto, user);
        JobHistoryEntity saved = jobHistoryRepository.save(entity);
        return ResponseEntity.ok(saved.toDTO());
    }

    // ✅ 구직내역 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJobHistory(@PathVariable Long id,
                                              @RequestBody JobHistoryDTO dto,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return jobHistoryRepository.findById(id).map(job -> {
            job.setStatus(dto.getStatus());
            job.setStartDate(dto.getStartDate());
            job.setEndDate(dto.getEndDate());
            job.setWorkplace(dto.getWorkplace());
            job.setJobTitle(dto.getJobTitle());
            jobHistoryRepository.save(job);
            return ResponseEntity.ok("수정 완료");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ✅ 구직내역 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJobHistory(@PathVariable Long id) {
        try {
            jobHistoryRepository.deleteById(id);
            return ResponseEntity.ok("삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }
}
