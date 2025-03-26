package com.example.bob.Repository;

import com.example.bob.Entity.ContestHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestHistoryRepository extends JpaRepository<ContestHistoryEntity, Long> {
}