package liveklass.notification.domain.notification.repository;

import liveklass.notification.domain.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 다중 인스턴스 환경에서 알림 1건을 선점하기 위한 조회.
 * DB {@code FOR UPDATE SKIP LOCKED}에 해당하는 잠금을 사용한다.
 */
public interface NotificationDispatchClaimRepository {

    //PENDING 중 발송 시각이 도래한 가장 작은 id 1건 + 잠금
    Optional<Notification> findAndLockNextPending(LocalDateTime now);

    //재시도 시각 지난 FAILED 1건 + 잠금
    Optional<Notification> findAndLockNextFailedRetryDue(LocalDateTime now, int maxRetryCount);
}
