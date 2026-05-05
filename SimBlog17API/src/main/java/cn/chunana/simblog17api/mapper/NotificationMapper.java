package cn.chunana.simblog17api.mapper;

import cn.chunana.simblog17api.dto.response.NotificationResponse;
import cn.chunana.simblog17api.entities.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                                   .id(notification.getId())
                                   .type(notification.getType())
                                   .targetType(notification.getTargetType())
                                   .targetId(notification.getTargetId())
                                   .title(notification.getTitle())
                                   .message(notification.getMessage())
                                   .status(notification.getStatus())
                                   .createTime(notification.getCreateTime())
                                   .readTime(notification.getReadTime())
                                   .build();
    }
}

