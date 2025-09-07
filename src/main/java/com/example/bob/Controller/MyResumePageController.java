package com.example.bob.Controller;

import com.example.bob.Entity.MyResume;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 이력서 목록 페이지를 보여주는 Controller (HTML 렌더링용)
 */
@Controller
@RequiredArgsConstructor
public class MyResumePageController {

    private final MyResumeService myResumeService;

    @GetMapping("/myresumelist")
    public String showMyResumeList(Model model) {
        Long memberId = 1L; // 고정 memberId
        List<MyResume> resumes = myResumeService.findAllByMemberId(memberId);

        // ✅ 로그 찍어보자
        System.out.println("⏺ 이력서 개수: " + resumes.size());
        resumes.forEach(r -> System.out.println("⏺ title: " + r.getTitle()));

        model.addAttribute("resumes", resumes);
        return "myresume_list";
    }

}
