package com.example.bob.Controller;

import com.example.bob.DTO.MyResumeDto;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 나만의 이력서 상세보기 페이지 Controller (HTML 렌더링용)
 */
@Controller
@RequiredArgsConstructor
public class MyResumeDetailPageController {

    private final MyResumeService myResumeService;

    @GetMapping("/myresume/{id}")
    public String showMyResumeDetail(
            @PathVariable Long id,
            @RequestParam(name = "preview", required = false, defaultValue = "false") boolean preview, // ✅ 추가된 부분!
            Model model) {

        // 👉 이력서 ID로 조회
        MyResumeDto resume = myResumeService.findByIdWithSections(id);

        if (resume == null) {
            // 예외 처리 (404 페이지로 넘길 수도 있음)
            return "error/404";
        }

        // ✅ 미리보기 여부를 모델에 추가
        model.addAttribute("resume", resume);
        model.addAttribute("isPreview", preview); // ✅ preview 값 전달

        return "myresume_detail"; // 👉 이 뷰파일에서 화면 그릴 거야!
    }
}
