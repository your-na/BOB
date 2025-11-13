package com.example.bob.Controller;

import com.example.bob.DTO.CoJobPostRequestDTO;
import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Service.CoJobPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.bob.DTO.CoJobPostResponseDTO;
import com.example.bob.DTO.CompanyJobStatDTO;
import com.example.bob.DTO.JobPostSummaryDTO;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;




@RestController
@RequestMapping("/api/cojobs")
public class CoJobPostController {

    @Autowired
    private CoJobPostService coJobPostService;

    // 구인글 등록
    @PostMapping
    public ResponseEntity<Long> createJobPost(@RequestBody CoJobPostRequestDTO dto) {
        Long jobPostId = coJobPostService.saveJobPost(dto); // ⭐ ID 반환 받기!
        return ResponseEntity.ok(jobPostId); // 👉 ID를 응답으로 보내기!
    }


    // 구인글 목록 조회
    @GetMapping
    public List<CoJobPostResponseDTO> getJobPosts() {
        List<CoJobPostResponseDTO> jobPosts = coJobPostService.getAllJobPosts();

        // 로그 출력 (선택사항)
        jobPosts.forEach(jobPost -> System.out.println("JobPost: " + jobPost));

        return jobPosts;
    }

    // 특정 공고 상세 정보 조회 (기업용 상세보기 페이지)
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobPostDetail(@PathVariable Long id) {
        // 서비스에서 공고 ID를 기준으로 상세 정보를 조회
        return ResponseEntity.ok(coJobPostService.getJobPostDetail(id));
    }

    // 이력서 양식 제목 포함된 공고 상세보기
    @GetMapping("/{id}/with-resumes")
    public ResponseEntity<?> getJobPostDetailWithResumes(@PathVariable Long id) {
        return ResponseEntity.ok(coJobPostService.getJobPostWithResumeTitles(id));
    }

    // 🔗 [GET] /api/cojobs/my-posts
    // ✅ 로그인된 기업이 작성한 모든 공고 목록을 조회하는 API
    // 상태(모집전, 모집중, 마감)는 서비스에서 자동 계산됨
    @GetMapping("/my-posts")
    public ResponseEntity<List<CoJobPostResponseDTO>> getMyJobPosts() {
        // 📞 서비스에서 현재 로그인 기업의 공고 목록 가져오기
        List<CoJobPostResponseDTO> myJobPosts = coJobPostService.getMyJobPosts();

        // 📤 클라이언트에 200 OK 상태로 응답 반환
        return ResponseEntity.ok(myJobPosts);
    }

    // 📊 [GET] 기업의 채용 통계 조회 (month 필터 추가)
    @GetMapping("/statistics/company")
    public ResponseEntity<CompanyJobStatDTO> getCompanyStatistics(
            @RequestParam(name = "month", required = false) Integer month) {

        CompanyJobStatDTO statDTO = coJobPostService.getCompanyJobStatistics(month); // ✅ 파라미터 전달
        return ResponseEntity.ok(statDTO);
    }

    @GetMapping("/statistics/months")
    public ResponseEntity<List<String>> getAvailableMonths() {
        return ResponseEntity.ok(coJobPostService.getAvailableJobPostMonths());
    }

    // ✅ 공고 삭제 요청을 처리하는 API
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJobPost(@PathVariable Long id) {
        System.out.println("🟡 삭제 요청 받음 - ID: " + id);  // ✅ 로그 추가

        try {
            coJobPostService.deleteJobPost(id);
            System.out.println("🟢 공고 삭제 성공");

            return ResponseEntity.ok("공고가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            System.err.println("❌ 공고 삭제 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("공고 삭제 실패: " + e.getMessage());
        }
    }








}
