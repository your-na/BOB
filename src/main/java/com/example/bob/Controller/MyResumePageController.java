package com.example.bob.Controller;

import com.example.bob.Entity.MyResume;
import com.example.bob.Service.MyResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Entity.UserEntity;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyResumePageController {

    private final MyResumeService myResumeService;
    private final UserRepository userRepository;


    /**
     * 이력서 만들기 HTML 렌더링
     */
    @GetMapping("/myresume")
    public String createResumePage(Model model) {
        // 로그인 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String memberId = auth.getName(); // 로그인된 사용자 ID

        // 사용자 정보 조회해서 넘겨주기
        UserEntity user = userRepository.findByUserIdLogin(memberId).orElse(null);
        model.addAttribute("user", user);

        model.addAttribute("resume", new MyResume()); // 비어있는 이력서
        return "myresume";
    }



    /**
     * 이력서 목록 페이지 렌더링
     */
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
