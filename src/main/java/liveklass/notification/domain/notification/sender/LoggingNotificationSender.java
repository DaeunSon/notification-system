package liveklass.notification.domain.notification.sender;

import liveklass.notification.domain.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void send(Notification notification) {
        log.info(
                "[Mock 발송] channel={}, notificationId={}, receiverId={}, title={}, content={}",
                notification.getChannel(),
                notification.getId(),
                notification.getReceiver().getId(),
                notification.getTitle(),
                notification.getContent()
        );
    }
}
