package com.example.bob.Controller;

import com.example.bob.Entity.MyResume;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        // 🔐 현재 로그인한 사용자의 ID(user_id_login)를 가져옴
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberId = auth.getName(); // 기본적으로 로그인 ID가 됨

        List<MyResume> resumes = myResumeService.findAllByMemberId(memberId);

        // ✅ 로그 찍기
        System.out.println("⏺ 이력서 개수: " + resumes.size());
        resumes.forEach(r -> System.out.println("⏺ title: " + r.getTitle()));

        model.addAttribute("resumes", resumes);
        return "myresume_list";
    }

}
