package liveklass.notification.domain.notification.dto.response;

import liveklass.notification.domain.notification.entity.Notification;

public record NotificationAcceptedResponse(

        Long notificationId,
        String message
) {

    public static NotificationAcceptedResponse accepted(Notification notification) {
        return new NotificationAcceptedResponse(
                notification.getId(),
                "알림 발송 요청이 접수되었습니다."
        );
    }
}
