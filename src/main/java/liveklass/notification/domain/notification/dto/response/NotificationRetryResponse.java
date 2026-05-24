package liveklass.notification.domain.notification.dto.response;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;

public record NotificationRetryResponse(

        Long notificationId,
        NotificationStatus status,
        int retryCount
) {
    public static NotificationRetryResponse from(Notification notification) {
        return new NotificationRetryResponse(
                notification.getId(),
                notification.getStatus(),
                notification.getRetryCount()
        );
    }
}
