package com.example.bob.Service;

import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Entity.NotificationEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Repository.NotificationRepository;
import com.example.bob.DTO.NotificationDTO;
import com.example.bob.DTO.UserDTO;
import com.example.bob.DTO.ProjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.bob.Entity.ProjectEntity;
import com.example.bob.Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository; // UserRepository 주입

    // 알림 수를 계산하는 메서드
    public int getUserNotificationCount(UserEntity userEntity) {
        // 사용자가 읽지 않은 알림의 개수를 계산 (알림 테이블에서)
        return notificationRepository.countByUserAndIsRead(userEntity, false);
    }

    // 기업 사용자 알림 수 조회
    public int getCompanyNotificationCount(CompanyEntity company){
        return notificationRepository.countByCompanyAndIsRead(company , false);
    }

    // 알림 목록을 가져오는 메서드 (페이지네이션 추가)
    public List<NotificationDTO> getNotifications(UserEntity userEntity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);  // 페이지 요청
        Page<NotificationEntity> notificationsPage = notificationRepository.findByUser(userEntity, pageable);

        // DTO로 변환하여 반환
        // DTO로 변환하여 반환
        return notificationsPage.getContent().stream()
                .map(notification -> new NotificationDTO(
                        notification.getId(),
                        notification.getMessage(),
                        notification.isRead(),
                        notification.getTimestamp(),
                        new UserDTO(notification.getUser().getUserId(), notification.getUser().getUserName()),  // 알림을 받은 사용자 정보
                        new UserDTO(notification.getSender().getUserId(), notification.getSender().getUserName()),  // 알림을 보낸 사용자 정보
                        new ProjectDTO(
                                notification.getProject().getId(),
                                notification.getProject().getTitle(),
                                notification.getProject().getCreatedBy(),
                                notification.getProject().getDescription(),
                                notification.getProject().getGoal(),
                                notification.getProject().getStartDate(),
                                notification.getProject().getEndDate(),
                                notification.getProject().getRecruitmentCount(),
                                notification.getProject().getCurrentParticipants(),
                                notification.getProject().getViews(),
                                notification.getProject().getLikes(),
                                notification.getProject().getStatus(),
                                notification.getProject().getRecruitmentPeriod(),
                                notification.getProject().getRecruitmentEndDate(),
                                notification.getProject().getRecruitmentStartDate()
                        ),  // 알림과 관련된 프로젝트 정보
                        notification.getLink()  // 알림 링크
                ))
                .collect(Collectors.toList());

    }

    // 알림을 읽음 상태로 변경하는 메서드
    public boolean markAsRead(Long id) {
        // 알림을 id로 찾기
        NotificationEntity notification = notificationRepository.findById(id).orElse(null);

        if (notification != null) {
            notification.setIsRead(true);  // 읽음 상태로 변경 (필드명이 isRead)
            notificationRepository.save(notification);  // 변경된 알림 저장

            // 추가적으로 알림 읽음 처리 로직이 필요한 경우 여기에 추가 가능 (예: 로깅, 다른 서비스 호출 등)

            return true;  // 성공적으로 읽음 상태로 변경됨
        }

        return false;  // 알림을 찾을 수 없으면 false 반환
    }

    // 알림 생성하는 메서드
    public void createNotification(UserEntity userEntity, ProjectEntity project, Long senderId) {
        // senderId로 UserEntity 객체를 찾아서 설정
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("보낸 사람을 찾을 수 없습니다."));

        // 알림 생성
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(userEntity);  // 알림을 받을 사용자 설정
        notification.setSender(sender);  // senderId로 찾은 UserEntity를 발송자 설정
        notification.setMessage(sender.getUserNick() + "님이 " + project.getTitle() + " 프로젝트에 팀원 신청을 보냈습니다.");  // 발송자의 닉네임과 프로젝트 제목 설정
        notification.setProject(project);  // 해당 프로젝트
        notification.setTimestamp(LocalDateTime.now());  // 알림 시간
        notification.setIsRead(false);  // 알림은 처음에는 읽지 않은 상태
        notificationRepository.save(notification);  // 알림 저장
    }

}



