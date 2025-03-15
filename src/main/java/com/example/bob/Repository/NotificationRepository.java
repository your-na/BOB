package com.example.bob.Repository;

import com.example.bob.Entity.NotificationEntity;
import com.example.bob.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // 사용자가 읽지 않은 알림의 개수를 카운트
    int countByUserAndIsRead(UserEntity userEntity, boolean isRead);

    // 사용자에 해당하는 모든 알림을 찾기
    List<NotificationEntity> findByUser(UserEntity userEntity);

    // 사용자가 읽지 않은 알림 목록을 가져오기
    List<NotificationEntity> findByUserAndIsRead(UserEntity userEntity, boolean isRead);


    // 사용자가 받는 알림을 Pageable로 가져오기
    Page<NotificationEntity> findByUser(UserEntity userEntity, Pageable pageable);
}
