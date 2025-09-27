package com.example.bob.Api;

import com.example.bob.DTO.EducationSimpleDTO;
import com.example.bob.Service.EducationService;
import com.example.bob.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/education-history")
@RequiredArgsConstructor
public class EducationApiController {

    private final EducationService educationService;

    // ✅ 학력 정보 저장
    @PostMapping("/save")
    public Long saveEducation(
            @AuthenticationPrincipal UserDetailsImpl userDetails, // 🔑 로그인 사용자 정보 주입
            @RequestBody EducationSimpleDTO dto) {

        Long userId = userDetails.getUserEntity().getUserId(); // 로그인한 사용자 ID 가져오기
        return educationService.save(userId, dto);
    }

    // ✅ 학력 목록 조회
    @GetMapping("/list")
    public List<EducationSimpleDTO> getEducations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserEntity().getUserId();
        return educationService.findByUserId(userId);
    }

    // ✅ 학력 삭제
    @DeleteMapping("/delete/{id}")
    public void deleteEducation(@PathVariable Long id) {
        educationService.deleteById(id);
    }
}
