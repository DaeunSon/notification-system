package liveklass.notification.domain.notification.dto.response;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;

public record NotificationStatusResponse(

        Long notificationId,
        NotificationStatus status
) {
    public static NotificationStatusResponse from(Notification notification) {
        return new NotificationStatusResponse(
                notification.getId(),
                notification.getStatus()
        );
    }
}
