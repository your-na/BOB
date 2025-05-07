package com.example.bob.Controller;

import com.example.bob.DTO.CoJobPostRequestDTO;
import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Service.CoJobPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.bob.DTO.CoJobPostResponseDTO;

import java.util.List;



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



}
