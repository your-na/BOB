package com.example.bob.Controller;

import com.example.bob.DTO.ContestDTO;
import com.example.bob.Entity.ContestEntity;
import com.example.bob.Service.ContestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ContestService contestService;

    @Autowired
    public AdminController(ContestService contestService) {
        this.contestService = contestService;
    }

    // ✅ 관리자용 공모전 목록 페이지
    @GetMapping("/contest")
    public String adminContestList(Model model) {
        List<ContestDTO> contests = contestService.getAllContests();
        model.addAttribute("contests", contests);
        return "ad_contest";  // ← templates/ad_contest.html
    }

    @GetMapping("/contest/{id}")
    public String showContestDetail(@PathVariable Long id, Model model) {
        ContestEntity entity = contestService.getById(id);
        ContestDTO dto = ContestDTO.fromEntity(entity);
        model.addAttribute("contest", dto);
        return "postcontest"; // postcontest.html
    }

}
