package com.example.bob.Repository;

import com.example.bob.Entity.JobHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobHistoryRepository extends JpaRepository<JobHistoryEntity, Long> {
    List<JobHistoryEntity> findByUserEntity_UserIdOrderByStartDateDesc(Long userId);
}
