package com.example.bob.Controller;

import com.example.bob.DTO.CoResumeRequestDTO;
import com.example.bob.Service.CoResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class CoResumeController {

    private final CoResumeService coResumeService;

    @Autowired
    public CoResumeController(CoResumeService coResumeService) {
        this.coResumeService = coResumeService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> saveResume(@RequestBody CoResumeRequestDTO requestDTO) {
        coResumeService.saveResume(requestDTO);

        // ✅ JSON 형식 응답 만들기
        Map<String, String> result = new HashMap<>();
        result.put("message", "이력서 저장 성공");

        return ResponseEntity.ok(result);
    }
}
