package com.example.bob.Controller;

import com.example.bob.Entity.ContestTeamEntity;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Entity.WbsEntity;
import com.example.bob.Repository.ContestTeamRepository;
import com.example.bob.Service.ProjectService;
import com.example.bob.Service.WbsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WbsController {

    private final WbsService wbsService;
    private final ProjectService projectService;
    private final ContestTeamRepository contestTeamRepository;


    // ✅ 특정 프로젝트/공모전 WBS 조회
    @ResponseBody
    @GetMapping("/api/wbs")
    public List<WbsEntity> getWbsList(@RequestParam String type, @RequestParam Long id) {
        return wbsService.getWbsList(type, id);
    }

    // ✅ WBS 저장 (전체 덮어쓰기 방식)
    @ResponseBody
    @PostMapping("/api/wbs")
    public void saveWbsList(@RequestBody List<WbsEntity> wbsList) {
        if (!wbsList.isEmpty()) {
            String type = wbsList.get(0).getType();
            Long targetId = wbsList.get(0).getTargetId();

            // 기존 데이터 제거 후 새로 저장 (덮어쓰기)
            wbsService.deleteWbsList(type, targetId);
            wbsService.saveWbsList(wbsList);
        }
    }

    // ✅ 프로젝트용 WBS 화면
    @GetMapping("/todocrud/project/{projectId}")
    public String showProjectWbsPage(@PathVariable Long projectId, Model model) {
        ProjectEntity project = projectService.getProjectById(projectId);
        model.addAttribute("project", project);
        return "todo_crud"; // 프로젝트 WBS 페이지
    }

}
