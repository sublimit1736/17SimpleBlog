package cn.chunana.simblog17api.services;

import cn.chunana.simblog17api.dto.response.NotificationResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationService {

    void createModerationNotification(Long userId,
                                      String targetType,
                                      Long targetId,
                                      String title,
                                      String message);

    PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    Optional<NotificationResponse> markAsRead(Long userId, Long notificationId);

    long markAllAsRead(Long userId);
}

