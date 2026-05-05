package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.dto.response.NotificationResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.entities.Notification;
import cn.chunana.simblog17api.mapper.NotificationMapper;
import cn.chunana.simblog17api.repository.NotificationRepository;
import cn.chunana.simblog17api.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final String TYPE_MODERATION = "MODERATION";

    private final NotificationRepository notificationRepository;

    @Override
    public void createModerationNotification(Long userId,
                                             String targetType,
                                             Long targetId,
                                             String title,
                                             String message) {
        if (userId == null) {
            return;
        }

        Notification notification = Notification.builder()
                                                .userId(userId)
                                                .type(TYPE_MODERATION)
                                                .targetType(targetType)
                                                .targetId(targetId)
                                                .title(title)
                                                .message(message)
                                                .status(Notification.STATUS_UNREAD)
                                                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        return PageResponse.from(
                notificationRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable)
                                      .map(NotificationMapper::toNotificationResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.STATUS_UNREAD);
    }

    @Override
    public Optional<NotificationResponse> markAsRead(Long userId, Long notificationId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId)
                                     .map(notification -> {
                                         if (notification.getStatus() == Notification.STATUS_UNREAD) {
                                             notification.setStatus(Notification.STATUS_READ);
                                             notification.setReadTime(LocalDateTime.now());
                                             notificationRepository.save(notification);
                                         }
                                         return NotificationMapper.toNotificationResponse(notification);
                                     });
    }

    @Override
    public long markAllAsRead(Long userId) {
        long updated = 0;
        for (Notification notification : notificationRepository.findByUserIdOrderByCreateTimeDesc(userId, Pageable.unpaged())) {
            if (notification.getStatus() == Notification.STATUS_UNREAD) {
                notification.setStatus(Notification.STATUS_READ);
                notification.setReadTime(LocalDateTime.now());
                updated++;
            }
        }
        return updated;
    }
}

