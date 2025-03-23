package com.example.bob.Controller;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TaskController {

    @GetMapping("/popup")
    public String openTaskPopup(Model model) {
//        // 예제: 백엔드에서 할 일 목록을 가져와서 전달
//        List<TaskDTO> tasks = taskService.getAllTasks();
//        model.addAttribute("tasks", tasks); 백엔드 추후 부탁
        return "task_popup"; // Thymeleaf 템플릿 (task_popup.html)
    }

    @GetMapping("/plan")
    public String planPopup() {
        return "plan"; // plan.html을 반환
    }
}

