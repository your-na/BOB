package com.example.bob.Api;

import com.example.bob.DTO.BasicInfoDTO;
import com.example.bob.Service.BasicInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/basic-info") // ✅ 공통 경로 prefix
@RequiredArgsConstructor
public class BasicInfoApiController {

    private final BasicInfoService basicInfoService;

    // ✅ 기본 정보 저장 (POST)
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody BasicInfoDTO dto,
                                                    Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        Long id = basicInfoService.save(userId, dto);
        return ResponseEntity.ok(Map.of("id", id));
    }

    // ✅ 기본 정보 삭제 (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        basicInfoService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ✅ 사용자 ID 추출 (현재는 임시값)
    private Long getUserIdFromPrincipal(Principal principal) {
        return 1L; // 🔁 나중에 로그인 연동되면 principal.getName() 등으로 변경
    }
}
