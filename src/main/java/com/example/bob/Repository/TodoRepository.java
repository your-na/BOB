package com.example.bob.Repository;

import com.example.bob.Entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {

    // ✅ 특정 날짜의 모든 할 일 조회 (todo_plan 용)
    List<TodoEntity> findByStartDate(String startDate);

    // ✅ popup 페이지에 띄울 할 일
    // 내 닉네임이 assignee에 포함된 모든 할 일을 가져옴 (공동 포함)
    @Query(value = "SELECT * FROM todo_entity WHERE FIND_IN_SET(:userNick, assignee)", nativeQuery = true)
    List<TodoEntity> findTodosForPopup(@Param("userNick") String userNick);

    // ✅ 날짜 + 로그인 유저 닉네임 기반 필터링 (todo_plan 용)
    @Query(value = "SELECT * FROM todo_entity " +
            "WHERE start_date = :startDate AND FIND_IN_SET(:userNick, assignee)", nativeQuery = true)
    List<TodoEntity> findByStartDateAndAssigneeContaining(
            @Param("startDate") String startDate,
            @Param("userNick") String userNick);


    List<TodoEntity> findByStartDateAndTargetIdAndTypeAndAssigneeContaining(
            String startDate, Long targetId, String type, String assignee);

    @Query("SELECT t FROM TodoEntity t WHERE t.targetId = :teamId AND t.type = '공모전' AND t.assignee LIKE %:userNick% AND :clickedDate BETWEEN t.startDate AND t.endDate")
    List<TodoEntity> findTodosByDateRangeForTeam(@Param("clickedDate") String clickedDate,
                                                 @Param("teamId") Long teamId,
                                                 @Param("userNick") String userNick);

}
