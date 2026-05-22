package liveklass.notification.domain.notification.dto.response;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.entity.NotificationType;

public record NotificationSummaryResponse(

        Long notificationId,
        NotificationType notificationType,
        String title,
        NotificationStatus status,
        NotificationChannel channel,
        boolean read
) {

    public static NotificationSummaryResponse from(Notification notification) {
        return new NotificationSummaryResponse(
                notification.getId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getStatus(),
                notification.getChannel(),
                notification.isRead()
        );
    }
}
