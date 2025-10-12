package com.example.bob.Api;

import com.example.bob.DTO.ProjectDTO;
import com.example.bob.Service.JjimProjectService;
import com.example.bob.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JjimProjectApiController {

    private final JjimProjectService jjimProjectService;

    // ✅ 로그인한 유저의 찜한 프로젝트 목록 조회
    @GetMapping("/jjim-projects")
    public List<ProjectDTO> getJjimProjects(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();  // 🔥 유저 ID
        return jjimProjectService.findJjimProjectsByUser(userId); // ✅ 서비스 호출 변경
    }
}
