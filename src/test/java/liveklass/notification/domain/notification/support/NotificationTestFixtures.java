package liveklass.notification.domain.notification.support;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationType;
import liveklass.notification.domain.user.entity.User;

import java.time.LocalDateTime;

public final class NotificationTestFixtures {

    private NotificationTestFixtures() {
    }

    public static Notification pending(User receiver) {
        return pending(receiver, NotificationChannel.EMAIL);
    }

    public static Notification pending(User receiver, NotificationChannel channel) {
        return Notification.createPending(
                receiver,
                NotificationType.ENROLLMENT_CONFIRMED,
                "course-1",
                channel,
                "title",
                "content",
                null
        );
    }

    public static Notification pending(User receiver, LocalDateTime scheduledAt) {
        return Notification.createPending(
                receiver,
                NotificationType.ENROLLMENT_CONFIRMED,
                "course-1",
                NotificationChannel.EMAIL,
                "title",
                "content",
                scheduledAt
        );
    }
}
