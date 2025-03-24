package com.example.bob.Repository;

import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Entity.NotificationEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // 일반 사용자
    int countByUserAndIsRead(UserEntity userEntity, boolean isRead); // 읽지 않은 알림의 개수를 카운트
    List<NotificationEntity> findByUser(UserEntity userEntity); // 사용자에 해당하는 모든 알림을 찾기
    List<NotificationEntity> findByUserAndIsRead(UserEntity userEntity, boolean isRead); // 사용자가 읽지 않은 알림 목록을 가져오기
    Page<NotificationEntity> findByUser(UserEntity userEntity, Pageable pageable); // 사용자가 받는 알림을 Pageable로 가져오기

    // 기업 사용자
    int countByCompanyAndIsRead(CompanyEntity companyEntity, boolean isRead); // 기업이 읽지 않은 알림 개수 카운트
    List<NotificationEntity> findByCompany(CompanyEntity companyEntity); // 모든 알림 찾기
    List<NotificationEntity> findByCompanyAndIsRead(CompanyEntity companyEntity, boolean isRead); // 기업 사용자가 읽지 않은 알림 목록을 가져오기
    Page<NotificationEntity> findByCompany(CompanyEntity companyEntity, Pageable pageable); // 기업 사용자가 받는 알림을 가져오기

    // 프로젝트 관련된 알림 삭제
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.project = :project")
    void deleteByProject(@Param("project") ProjectEntity project);
}
