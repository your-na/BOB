package com.example.bob.Service;

import com.example.bob.DTO.TodoRequestDto;
import com.example.bob.Entity.ContestTeamEntity;
import com.example.bob.Entity.TodoEntity;
import com.example.bob.Repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.ProjectService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class    TodoService {

    private final TodoRepository todoRepository;
    private final Logger logger = LoggerFactory.getLogger(TodoService.class);
    private final ProjectService projectService;

    // 할 일 저장 메서드
    public TodoEntity save(TodoRequestDto dto, UserEntity user, boolean isHost) {
        String assigneeProcessed = resolveAssignee(
                dto.getAssignee(),
                dto.getWorkspace(),
                user.getUserNick(),
                isHost
        );

        TodoEntity todo = TodoEntity.builder()
                .title(dto.getTitle())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .assignee(assigneeProcessed)
                .workspace(dto.getWorkspace())
                .completed(false)
                .type(resolveType(dto.getWorkspace()))  // workspace에 맞춰 type 설정
                .build();

        return todoRepository.save(todo);
    }

    // '개인' 할 일 저장을 위한 별도 메서드
    public TodoEntity savePersonalTodo(TodoRequestDto dto, UserEntity user) {
        // 개인 할 일의 경우에는 `workspace`가 "개인"으로 설정되고, assignee는 항상 "나" (List로 처리)
        TodoEntity todo = TodoEntity.builder()
                .title(dto.getTitle())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .assignee(user.getUserNick())
                .workspace("개인")  // workspace는 "개인"
                .completed(false)
                .type("개인")  // type도 "개인"으로 설정
                .build();

        return todoRepository.save(todo);  // 할 일 저장
    }




    // 팝업용 할 일 조회
    public List<TodoEntity> getTodosForPopup(String userNick) {
        return todoRepository.findTodosForPopup(userNick);
    }

    // 날짜별 할 일 조회
    public List<TodoEntity> findByDate(String date) {
        return todoRepository.findByStartDate(date); // 변경된 필드명 사용
    }

    // 완료 상태 업데이트
    @Transactional
    public void updateCompletion(Long id, boolean completed) {
        TodoEntity todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));

        System.out.println("🛠️ 기존 상태: " + todo.isCompleted());
        todo.setCompleted(completed);
        todoRepository.save(todo);

        System.out.println("✅ 업데이트 완료! id: " + id + ", 새 상태: " + todo.isCompleted());
    }

    // 담당자 처리 메서드
    private String resolveAssignee(String selectedAssignee, String workspace, String currentUserNick, boolean isHost) {
        if (!isHost) {
            return currentUserNick;
        }

        if ("공동".equals(selectedAssignee)) {
            List<String> allNicknames = projectService.getProjectMemberNicknames(workspace);
            if (!allNicknames.contains(currentUserNick)) {
                allNicknames.add(currentUserNick);
            }
            return String.join(",", allNicknames);
        } else if ("나".equals(selectedAssignee)) {
            return currentUserNick;
        } else {
            return selectedAssignee;
        }
    }

    // workspace 값에 따라 type을 설정하는 메서드
    private String resolveType(String workspace) {
        if ("개인".equals(workspace)) {
            return "개인";
        } else if ("공모전".equals(workspace)) {
            return "공모전";
        } else {
            return "프로젝트"; // 기본값: 프로젝트
        }
    }

    // 로그인한 유저의 닉네임이 포함된 할 일만 조회
    public List<TodoEntity> findByDateAndUserNick(String date, String userNick) {
        return todoRepository.findByStartDateAndAssigneeContaining(date, userNick);
    }


    //--------------------------------------------------------
    // 공모전용!!!!!!!!!!!!!!!!!!!!!!!!

    public TodoEntity saveForContest(TodoRequestDto dto, UserEntity user, boolean isHost, ContestTeamEntity team) {
        String assigneeProcessed = resolveAssignee(
                dto.getAssignee(),
                dto.getWorkspace(),
                user.getUserNick(),
                isHost
        );

        TodoEntity todo = TodoEntity.builder()
                .title(dto.getTitle())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .assignee(assigneeProcessed)
                .workspace(team.getTeamName())  // 팀 이름을 workspace로 저장
                .completed(false)
                .type("공모전")
                .targetId(team.getId())
                .build();

        return todoRepository.save(todo);
    }

    public List<TodoEntity> findByDateAndTeam(String date, Long teamId, UserEntity user) {
        return todoRepository.findByStartDateAndTargetIdAndTypeAndAssigneeContaining(
                date, teamId, "공모전", user.getUserNick());
    }
}
