package com.example.bob.Repository;

import com.example.bob.Entity.CoJobPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;



public interface CoJobPostRepository extends JpaRepository<CoJobPostEntity, Long> {

    List<CoJobPostEntity> findByCompany_CompanyId(Long companyId);

}
