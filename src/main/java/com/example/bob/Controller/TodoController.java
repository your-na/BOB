package com.example.bob.Controller;

import com.example.bob.DTO.TodoRequestDto;
import com.example.bob.Entity.TodoEntity;
import com.example.bob.Service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.bob.security.UserDetailsImpl;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.ProjectService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import java.util.List;


@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@CrossOrigin // 프론트와 연결 위해 필요
public class TodoController {

    private final TodoService todoService;
    private final Logger logger = LoggerFactory.getLogger(TodoController.class);
    private final ProjectService projectService;


    @PostMapping
    public TodoEntity createTodo(@RequestBody TodoRequestDto dto,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserEntity user = userDetails.getUserEntity();

        // workspace가 "개인"인 경우에는 프로젝트를 찾지 않고 바로 개인 할일을 등록하도록 처리
        if ("개인".equals(dto.getWorkspace())) {
            return todoService.savePersonalTodo(dto, user);
        }

        // 주최자인지 판단하는 메서드는 ProjectService에 구현한다고 가정
        boolean isHost = projectService.isUserHost(dto.getWorkspace(), user.getUserNick());

        return todoService.save(dto, user, isHost);
    }


    @GetMapping
    public List<TodoEntity> getTodos(@RequestParam String date) {
        return todoService.findByDate(date); // startDate 기준으로 조회
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> updateTodoCompletion(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> requestBody) {

        boolean completed = requestBody.get("completed");
        System.out.println("📦 [PATCH] 체크 상태 변경 요청! ✅ id: " + id + ", completed: " + completed);

        todoService.updateCompletion(id, completed);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popup")
    @ResponseBody
    public List<TodoEntity> getPopupTodos(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return todoService.getTodosForPopup(userDetails.getUserEntity().getUserNick());
    }


}
