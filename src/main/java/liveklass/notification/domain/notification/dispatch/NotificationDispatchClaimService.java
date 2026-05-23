package liveklass.notification.domain.notification.dispatch;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.repository.NotificationDispatchClaimRepository;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 알림 1건을 DB 잠금(SKIP LOCKED)으로 선점한 뒤 PROCESSING으로 전이한다.
 * 선점과 상태 변경을 짧은 트랜잭션에서 끝내 다중 인스턴스 중복 처리를 방지한다.
 */
@Service
@RequiredArgsConstructor
public class NotificationDispatchClaimService {

    private final NotificationDispatchClaimRepository notificationDispatchClaimRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Notification> claimNextPending() {
        return notificationDispatchClaimRepository.findAndLockNextPending()
                .map(notification -> {
                    notification.startProcessing();
                    return notificationRepository.save(notification);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Notification> claimNextFailedRetryDue() {
        return notificationDispatchClaimRepository.findAndLockNextFailedRetryDue(
                        LocalDateTime.now(),
                        Notification.MAX_RETRY_COUNT
                )
                .map(notification -> {
                    notification.startProcessingForRetry();
                    return notificationRepository.save(notification);
                });
    }
}
