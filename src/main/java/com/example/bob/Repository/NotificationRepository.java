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
    int countByUserAndIsRead(UserEntity userEntity, boolean isRead);

    List<NotificationEntity> findByUser(UserEntity userEntity);

    List<NotificationEntity> findByUserAndIsRead(UserEntity userEntity, boolean isRead);

    Page<NotificationEntity> findByUser(UserEntity userEntity, Pageable pageable);

    // 기업 사용자
    int countByCompanyAndIsRead(CompanyEntity companyEntity, boolean isRead);

    List<NotificationEntity> findByCompany(CompanyEntity companyEntity);

    List<NotificationEntity> findByCompanyAndIsRead(CompanyEntity companyEntity, boolean isRead);

    Page<NotificationEntity> findByCompany(CompanyEntity companyEntity, Pageable pageable);

    // 프로젝트 관련된 알림 삭제
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.project = :project")
    void deleteByProject(@Param("project") ProjectEntity project);

    // ✅ 수락/거절 시 알림 1건 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM NotificationEntity n WHERE n.sender.userNick = :sender " +
            "AND n.user = :receiver " +
            "AND n.project.title = :projectTitle")
    void deleteBySenderAndReceiverAndProjectTitle(@Param("sender") String sender,
                                                  @Param("receiver") UserEntity receiver,
                                                  @Param("projectTitle") String projectTitle);

    // ✅ 사용자 알림 전체 삭제
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
}
