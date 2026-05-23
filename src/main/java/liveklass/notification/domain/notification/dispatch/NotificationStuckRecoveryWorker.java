package liveklass.notification.domain.notification.dispatch;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 스턱 알림 1건 복구. 별도 빈으로 분리해 {@code @Transactional} 프록시가 적용되도록 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationStuckRecoveryWorker {

    private static final String STUCK_REASON = "Processing timeout (stuck recovery)";

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverOne(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElse(null);
        if (notification == null || notification.getStatus() != NotificationStatus.PROCESSING) {
            return;
        }

        notification.recoverFromStuckProcessing(STUCK_REASON);
        notificationRepository.save(notification);
        log.warn("PROCESSING 스턱 복구 notificationId={} status={} stuckRecoveryCount={}",
                notificationId, notification.getStatus(), notification.getStuckRecoveryCount());
    }
}
