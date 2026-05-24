package liveklass.notification.domain.notification.event;

import liveklass.notification.domain.notification.dto.CreateNotificationRequest;

/**
 * 비즈니스 TX 종료(commit/rollback) 후 알림 PENDING 생성을 요청할 때 발행한다.
 */
public record NotificationRequestedEvent(CreateNotificationRequest request) {
}
