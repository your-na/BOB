package com.example.bob.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    @GetMapping("/search")
    public String searchResult(@RequestParam("kw") String keyword, Model model) {
        // TODO: 여기에서 실제 검색 로직을 추가하세요.
        // 예: 공모전, 프로젝트, 게시글, 프로필에서 keyword 포함 여부로 필터링 등

        model.addAttribute("kw", keyword);
        // model.addAttribute("contests", contestService.search(keyword));
        // model.addAttribute("projects", projectService.search(keyword));
        // model.addAttribute("posts", postService.search(keyword));
        // model.addAttribute("profiles", profileService.search(keyword));

        return "search_result"; // templates/search_result.html
    }
}
