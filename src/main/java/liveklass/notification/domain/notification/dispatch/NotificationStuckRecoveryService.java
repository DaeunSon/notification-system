package liveklass.notification.domain.notification.dispatch;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 오래 PROCESSING에 머문 알림을 조회하고, 건별 복구는 {@link NotificationStuckRecoveryWorker}에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class NotificationStuckRecoveryService {

    private final NotificationRepository notificationRepository;
    private final NotificationStuckRecoveryWorker notificationStuckRecoveryWorker;

    public void recoverStuckProcessingNotifications() {
        LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(NotificationDispatchConstants.STUCK_PROCESSING_THRESHOLD_MINUTES);

        List<Notification> stuck = notificationRepository.findByStatusAndProcessingStartedAtBefore(
                NotificationStatus.PROCESSING,
                threshold
        );

        for (Notification notification : stuck) {
            notificationStuckRecoveryWorker.recoverOne(notification.getId());
        }
    }
}
