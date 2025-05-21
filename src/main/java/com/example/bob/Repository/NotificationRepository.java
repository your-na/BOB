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

    void deleteByContestTeamIdAndUser(Long teamId, UserEntity user);

    Page<NotificationEntity> findByUserAndIsHiddenFalse(UserEntity user, Pageable pageable);

    int countByUserAndIsReadFalseAndIsHiddenFalse(UserEntity user);

    List<NotificationEntity> findByUserAndIsHiddenFalse(UserEntity user);

    List<NotificationEntity> findByContestTeamIdAndUserAndIsHiddenFalse(Long teamId, UserEntity user);


    // 프로젝트 관련된 알림 삭제
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isHidden = true WHERE n.project = :project")
    void hideByProject(@Param("project") ProjectEntity project);


    // ✅ 수락/거절 시 알림 1건 삭제
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isHidden = true WHERE n.sender.userNick = :sender AND n.user = :receiver AND n.project.title = :projectTitle")
    void hideBySenderAndReceiverAndProjectTitle(@Param("sender") String sender,
                                                @Param("receiver") UserEntity receiver,
                                                @Param("projectTitle") String projectTitle);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isHidden = true WHERE n.contestTeam.id = :teamId AND n.user = :user")
    void hideByContestTeamIdAndUser(@Param("teamId") Long teamId, @Param("user") UserEntity user);

}
