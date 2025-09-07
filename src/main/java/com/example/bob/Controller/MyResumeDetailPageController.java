package com.example.bob.Controller;

import com.example.bob.Entity.MyResume;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 나만의 이력서 상세보기 페이지 Controller (HTML 렌더링용)
 */
@Controller
@RequiredArgsConstructor
public class MyResumeDetailPageController {

    private final MyResumeService myResumeService;

    @GetMapping("/myresume/{id}")
    public String showMyResumeDetail(@PathVariable Long id, Model model) {
        // 👉 이력서 ID로 조회
        MyResume resume = myResumeService.findByIdWithSections(id);

        if (resume == null) {
            // 예외 처리 (404 페이지로 넘길 수도 있음)
            return "error/404";
        }

        model.addAttribute("resume", resume);
        return "myresume_detail"; // 👉 이 뷰파일에서 화면 그릴 거야!
    }
}
