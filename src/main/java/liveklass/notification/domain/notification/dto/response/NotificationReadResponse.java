package liveklass.notification.domain.notification.dto.response;

import liveklass.notification.domain.notification.entity.Notification;

public record NotificationReadResponse(

        Long notificationId,
        boolean read
) {
    public static NotificationReadResponse from(Notification notification) {
        return new NotificationReadResponse(
                notification.getId(),
                notification.isRead()
        );
    }
}
