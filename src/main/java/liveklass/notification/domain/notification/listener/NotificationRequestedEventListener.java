package liveklass.notification.domain.notification.listener;

import liveklass.notification.domain.notification.dispatch.NotificationDispatchTrigger;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.event.NotificationRequestedEvent;
import liveklass.notification.domain.notification.service.NotificationCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * 비즈니스 TX 커밋 후 알림 PENDING을 생성한다.
 * IN_APP은 생성 직후 dispatch를 비동기 트리거하고, EMAIL은 스케줄러에 맡긴다.
 */
@Component
@RequiredArgsConstructor
public class NotificationRequestedEventListener {

    private final NotificationCreationService notificationCreationService;
    private final NotificationDispatchTrigger notificationDispatchTrigger;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationRequested(NotificationRequestedEvent event) {
        var request = event.request();
        notificationCreationService.createPending(request);

        if (request.channel() == NotificationChannel.IN_APP && isDispatchDue(request.scheduledAt())) {
            notificationDispatchTrigger.triggerPendingDispatch();
        }
    }

    private boolean isDispatchDue(LocalDateTime scheduledAt) {
        return scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now());
    }
}
