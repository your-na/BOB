package com.example.bob.Repository;

import com.example.bob.Entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    // 회원가입
    Boolean existsByCoIdLogin(String coIdLogin);

    //로그인
    Optional<CompanyEntity> findByCoIdLogin(String coIdLogin);
}
