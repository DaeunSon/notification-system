package liveklass.notification.domain.notification.dispatch;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 스케줄러용 발송 오케스트레이션. 조회·루프만 담당하고,
 * 건별 트랜잭션은 {@link NotificationDispatchWorker}에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatchWorker notificationDispatchWorker;

    public void dispatchPendingNotifications() {
        List<Notification> pending =
                notificationRepository.findByStatusOrderByIdAsc(NotificationStatus.PENDING);

        for (Notification notification : pending) {
            notificationDispatchWorker.dispatch(notification);
        }
    }

    public void dispatchFailedRetryDueNotifications() {
        List<Notification> failedRetryDue =
                notificationRepository.findByStatusAndNextRetryAtBeforeAndRetryCountLessThan(
                        NotificationStatus.FAILED,
                        LocalDateTime.now(),
                        Notification.MAX_RETRY_COUNT
                );

        for (Notification notification : failedRetryDue) {
            notificationDispatchWorker.dispatchRetry(notification);
        }
    }
}
