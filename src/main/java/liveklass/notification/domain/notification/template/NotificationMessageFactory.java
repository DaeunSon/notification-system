package liveklass.notification.domain.notification.template;

import liveklass.notification.domain.notification.dto.RenderedNotificationMessage;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageFactory {

    public RenderedNotificationMessage render(
            NotificationType notificationType,
            NotificationChannel channel,
            String referenceId
    ) {
        return switch (channel) {
            case EMAIL -> renderEmail(notificationType, referenceId);
            case IN_APP -> renderInApp(notificationType, referenceId);
        };
    }

    private RenderedNotificationMessage renderEmail(NotificationType type, String referenceId) {
        String body = """
                안녕하세요,

                %s

                감사합니다.
                LiveKlass 알림팀
                """.formatted(type.formatContent(referenceId));

        return new RenderedNotificationMessage(type.getTitle(), body);
    }

    private RenderedNotificationMessage renderInApp(NotificationType type, String referenceId) {
        return new RenderedNotificationMessage(type.getTitle(), type.formatContent(referenceId));
    }
}
