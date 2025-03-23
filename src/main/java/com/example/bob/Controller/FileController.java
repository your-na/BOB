package com.example.bob.Controller;

import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.UserProjectEntity;
import com.example.bob.Repository.ProjectRepository;
import com.example.bob.Repository.UserProjectRepository;
import com.example.bob.Service.ProjectService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Optional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/file")  // 🔥 파일 관련 공통 경로 설정
@RequiredArgsConstructor
public class FileController {

    private final String projectFilePath = "C:/uploads/project/"; // 파일 저장 경로
    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService; // ✅ ProjectService 추가
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    // ✅ 프로젝트 파일 제출 (업로드)
    @PostMapping("/project/submit")
    public ResponseEntity<String> submitProjectFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            logger.debug("파일 제출 요청 시작: 프로젝트 ID: " + projectId + ", 파일명: " + file.getOriginalFilename());

            // 인증된 사용자 정보 가져오기
            UserEntity user = userDetails.getUserEntity();

            // 프로젝트 정보 가져오기
            ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("❌ 해당 프로젝트를 찾을 수 없습니다."));

            // 파일명 및 저장 경로 설정
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // 중복 방지

            // submitProjectFile 메서드를 호출하여 파일 제출 처리
            projectService.submitProjectFile(user, project, fileName, file); // 호출

            // 프로젝트 상태를 "완료"로 변경
            project.setStatus("완료");
            projectRepository.save(project);  // 프로젝트 상태 업데이트

            logger.debug("파일 제출 완료: " + fileName);

            return ResponseEntity.ok("✅ 파일 제출이 완료되었습니다!");
        } catch (Exception e) {
            logger.error("파일 제출 중 오류 발생: " + e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ 파일 제출 중 오류 발생: " + e.getMessage());
        }
    }





    // ✅ 제출된 파일 정보를 반환하는 API
    @GetMapping("/submitted-files")
    public ResponseEntity<Map<Long, String>> getSubmittedFiles(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            UserEntity user = userDetails.getUserEntity();

            // 사용자가 제출한 파일 목록 가져오기
            Map<Long, String> submittedFiles = userProjectRepository.findByUser(user).stream()
                    .filter(submission -> submission.getSubmittedFileName() != null) // 제출한 파일만 가져오기
                    .collect(Collectors.toMap(
                            submission -> submission.getProject().getId(), // 프로젝트 ID
                            UserProjectEntity::getSubmittedFileName // 제출한 파일명
                    ));

            return ResponseEntity.ok(submittedFiles);
        } catch (Exception e) {
            logger.error("제출된 파일 조회 중 오류 발생: " + e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.emptyMap());
        }
    }

    // ✅ 프로젝트 파일 다운로드
    @GetMapping("/project/download/{fileName}")
    public ResponseEntity<Resource> downloadProjectFile(@PathVariable String fileName) {
        return downloadFile(projectFilePath, fileName);
    }

    // 📌 공통 파일 다운로드 로직
    private ResponseEntity<Resource> downloadFile(String basePath, String fileName) {
        try {
            Path filePath = Paths.get(basePath + fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("❌ 파일을 찾을 수 없습니다: " + fileName);
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("❌ 파일 다운로드 실패: " + fileName, e);
        }
    }

    // ✅ 🔥 사용자가 제출한 프로젝트 목록 조회 API 추가
    @GetMapping("/project/submitted")
    public ResponseEntity<List<UserProjectEntity>> getSubmittedProjects(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();
        List<UserProjectEntity> submittedProjects = userProjectRepository.findByUser_UserIdAndSubmittedFileNameIsNotNull(user.getUserId());

        if (submittedProjects.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(submittedProjects);
    }
}
