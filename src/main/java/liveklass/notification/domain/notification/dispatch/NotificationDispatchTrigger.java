package liveklass.notification.domain.notification.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationDispatchTrigger {

    private final NotificationDispatchService notificationDispatchService;

    @Async
    public void triggerPendingDispatch() {
        notificationDispatchService.dispatchPendingNotifications();
    }
}
