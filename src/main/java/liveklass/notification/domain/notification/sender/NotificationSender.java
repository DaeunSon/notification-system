package liveklass.notification.domain.notification.sender;

import liveklass.notification.domain.notification.entity.Notification;

/**
 * 알림 채널별 실제 발송 담당. 운영에서는 SMTP, FCM 등 구현체로 교체한다.
 */
public interface NotificationSender {

    void send(Notification notification);
}
