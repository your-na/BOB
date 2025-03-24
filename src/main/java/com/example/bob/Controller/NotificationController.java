package com.example.bob.Controller;

import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.NotificationService;
import com.example.bob.security.UserDetailsImpl;
import com.example.bob.DTO.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.example.bob.Entity.NotificationEntity;





@RestController
@RequestMapping("/api/notifications")  // API 엔드포인트 경로
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 알림 수 가져오기
    @GetMapping("/count")
    public Map<String, Integer> getNotificationCount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return Map.of("notificationCount", 0);  // 로그인하지 않은 경우
        }
        UserEntity userEntity = userDetails.getUserEntity();
        int notificationCount = notificationService.getNotificationCount(userEntity);
        return Map.of("notificationCount", notificationCount);  // 알림 수 반환
    }

    @GetMapping("/list")
    public List<Map<String, Object>> getNotifications(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                      @RequestParam int page,
                                                      @RequestParam int size) {
        System.out.println("Page: " + page + ", Size: " + size);
        UserEntity userEntity = userDetails.getUserEntity();

        // NotificationService에서 NotificationDTO 목록을 가져옴
        List<NotificationDTO> notificationDTOs = notificationService.getNotifications(userEntity, page, size);

        // 클라이언트에게 반환할 데이터를 준비
        List<Map<String, Object>> responseList = new ArrayList<>();

        // NotificationDTO 객체를 Map 형태로 변환하여 응답 리스트에 추가
        for (NotificationDTO dto : notificationDTOs) {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("id", dto.getId());  // 알림 ID
            notificationData.put("message", dto.getMessage());  // 알림 메시지
            notificationData.put("isRead", dto.isRead());  // 읽음 여부
            notificationData.put("timestamp", dto.getTimestamp());  // 알림 시간
            notificationData.put("link", dto.getLink());  // 알림 링크

            responseList.add(notificationData);
        }

        // 응답을 클라이언트에게 반환
        return responseList;
    }



    // 알림 읽음 상태로 변경
    @PatchMapping("/mark-as-read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        boolean success = notificationService.markAsRead(id);
        if (success) {
            return ResponseEntity.ok("알림이 읽음 상태로 변경되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("알림을 찾을 수 없습니다.");
        }
    }
}
