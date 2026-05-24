package liveklass.notification.domain.notification.listener;

import liveklass.notification.domain.notification.event.NotificationRequestedEvent;
import liveklass.notification.domain.notification.service.NotificationCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 비즈니스 TX 커밋 후 알림 PENDING을 생성한다.
 * 수강 거절·취소도 비즈니스 결과로 커밋되므로 AFTER_COMMIT 하나로 처리한다.
 * 시스템 오류(DB 등)로 인한 롤백은 알림 생성 대상이 아니다.
 */
@Component
@RequiredArgsConstructor
public class NotificationRequestedEventListener {

    private final NotificationCreationService notificationCreationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationRequested(NotificationRequestedEvent event) {
        notificationCreationService.createPending(event.request());
    }
}
