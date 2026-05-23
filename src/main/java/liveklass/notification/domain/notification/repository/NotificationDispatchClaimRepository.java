package liveklass.notification.domain.notification.repository;

import liveklass.notification.domain.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 다중 인스턴스 환경에서 알림 1건을 선점하기 위한 조회.
 * DB {@code FOR UPDATE SKIP LOCKED}에 해당하는 잠금을 사용한다.
 */
public interface NotificationDispatchClaimRepository {

    Optional<Notification> findAndLockNextPending();

    Optional<Notification> findAndLockNextFailedRetryDue(LocalDateTime now, int maxRetryCount);
}
