package com.example.bob.Repository;

import com.example.bob.Entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
    List<TodoEntity> findByStartDate(String startDate); // ✅ 이것만 있으면 됨!

    // ✅ 팝업에 띄울 할 일 (주최자만 공동 할 일 보이게)
    @Query("SELECT t FROM TodoEntity t WHERE " +
            "t.assignee = :userNick OR " +
            "(t.workspace = '개인' AND t.assignee = :userNick) OR " +
            "(t.assignee = '공동' AND :userNick = (SELECT p.createdBy FROM ProjectEntity p WHERE p.title = t.workspace))")
    List<TodoEntity> findTodosForPopup(@Param("userNick") String userNick);
}
