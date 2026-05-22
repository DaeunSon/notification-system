package liveklass.notification.domain.notification.dto.response;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.entity.NotificationType;

public record NotificationDetailResponse(

        Long notificationId,
        Long receiverId,
        NotificationType notificationType,
        String referenceId,
        NotificationChannel channel,
        String title,
        String content,
        NotificationStatus status,
        boolean read
) {

    public static NotificationDetailResponse from(Notification notification) {
        return new NotificationDetailResponse(
                notification.getId(),
                notification.getReceiver().getId(),
                notification.getNotificationType(),
                notification.getReferenceId(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getContent(),
                notification.getStatus(),
                notification.isRead()
        );
    }
}
