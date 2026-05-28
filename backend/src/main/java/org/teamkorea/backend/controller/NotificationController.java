package org.teamkorea.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.teamkorea.backend.config.UserDetailsImpl;
import org.teamkorea.backend.domain.User;
import org.teamkorea.backend.dto.NotificationResponseDto;
import org.teamkorea.backend.service.NotificationService;
// import org.teamkorea.backend.security.UserDetailsImpl; // 프로젝트의 UserDetails에 맞게 수정해주세요!

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 1. 알림 목록 조회 (404 에러 해결의 핵심!)
     * GET /api/notifications?onlyUnread=false&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "false") boolean onlyUnread,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDto> response = notificationService.getNotifications(user, onlyUnread, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 안 읽은 알림 개수 조회 (상단 배지용)
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        long unreadCount = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(Map.of("count", unreadCount));
    }

    /**
     * 3. 알림 단건 읽음 처리
     * PATCH /api/notifications/{notificationId}/read
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @PathVariable Long notificationId, 
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        User user = userDetails.getUser();
        NotificationResponseDto response = notificationService.markAsRead(notificationId, user);
        
        return ResponseEntity.ok(response);
    }
}