package com.example.bob.Repository;

import com.example.bob.Entity.BasicInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

// ✅ 기본 정보 저장/조회/삭제를 위한 JPA 리포지토리
public interface BasicInfoRepository extends JpaRepository<BasicInfo, Long> {

    // ✅ 사용자 ID로 기본 정보 조회 (한 명당 하나만 있다고 가정)
    Optional<BasicInfo> findByUserId(Long userId);

    List<BasicInfo> findAllByUserId(Long userId);

}
