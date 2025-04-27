package com.example.bob.Controller;

import com.example.bob.DTO.CoJobPostRequestDTO;
import com.example.bob.Service.CoJobPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cojobs")
public class CoJobPostController {

    @Autowired
    private CoJobPostService coJobPostService;

    @PostMapping
    public ResponseEntity<String> createJobPost(@RequestBody CoJobPostRequestDTO dto) {
        coJobPostService.saveJobPost(dto);
        return ResponseEntity.ok("구인글 등록 성공");
    }
}
