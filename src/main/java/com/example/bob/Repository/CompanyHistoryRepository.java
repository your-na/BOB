package com.example.bob.Repository;

import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Entity.CompanyHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyHistoryRepository extends JpaRepository<CompanyHistoryEntity, Long>{

}
