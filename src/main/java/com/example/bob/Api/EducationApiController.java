package com.example.bob.Api;

import com.example.bob.DTO.EducationSimpleDTO;
import com.example.bob.Service.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/education-history")
@RequiredArgsConstructor
public class EducationApiController {

    private final EducationService educationService;

    // ✅ 학력 정보 저장
    @PostMapping("/save")
    public Long saveEducation(@RequestBody EducationSimpleDTO dto, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal); // 로그인 유저 ID 추출
        return educationService.save(userId, dto);
    }

    // ✅ 학력 목록 조회
    @GetMapping("/list")
    public List<EducationSimpleDTO> getEducations(Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        return educationService.findByUserId(userId);
    }

    // ✅ 학력 삭제
    @DeleteMapping("/delete/{id}")
    public void deleteEducation(@PathVariable Long id) {
        educationService.deleteById(id);
    }

    // ✅ 테스트용 고정 ID (차후 principal에서 연동)
    private Long getUserIdFromPrincipal(Principal principal) {
        return 1L; // 개발 중이라 임시로 고정
    }
}
