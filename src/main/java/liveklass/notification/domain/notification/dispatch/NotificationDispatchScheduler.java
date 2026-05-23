package liveklass.notification.domain.notification.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatchScheduler {

    private final NotificationDispatchService notificationDispatchService;
    private final NotificationStuckRecoveryService notificationStuckRecoveryService;

    @Scheduled(fixedDelay = 5000)
    public void pollPendingNotifications() {
        notificationDispatchService.dispatchPendingNotifications();
    }

    @Scheduled(fixedDelay = 5000)
    public void pollFailedRetryDueNotifications() {
        notificationDispatchService.dispatchFailedRetryDueNotifications();
    }

    @Scheduled(fixedDelay = 60_000)
    public void recoverStuckProcessingNotifications() {
        notificationStuckRecoveryService.recoverStuckProcessingNotifications();
    }
}
