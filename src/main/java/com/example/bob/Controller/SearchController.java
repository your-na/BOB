package com.example.bob.Controller;

import com.example.bob.Entity.ContestEntity;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.ContestService;
import com.example.bob.Service.ProjectService;
import com.example.bob.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final ContestService contestService;
    private final ProjectService projectService;
    private final UserService userService;

    @GetMapping("/search")
    public String searchResult(@RequestParam("kw") String keyword, Model model) {
        List<ContestEntity> contests = contestService.search(keyword);
        List<ProjectEntity> projects = projectService.search(keyword);
        List<UserEntity> profiles = userService.search(keyword);
        // 게시글은 아직 미구현

        model.addAttribute("kw", keyword);
        model.addAttribute("contests", contests != null ? contests : List.of());
        model.addAttribute("projects", projects != null ? projects : List.of());
        model.addAttribute("profiles", profiles != null ? profiles : List.of());
        model.addAttribute("posts", List.of());
        model.addAttribute("totalResults",
                (contests != null ? contests.size() : 0) +
                        (projects != null ? projects.size() : 0) +
                        (profiles != null ? profiles.size() : 0)
        );

        return "search_result";
    }

}
