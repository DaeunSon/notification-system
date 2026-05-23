package liveklass.notification.domain.notification.dispatch;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 1건 단위 발송 처리. 별도 빈으로 분리해 {@code @Transactional} 프록시가 적용되도록 한다.
 */
@Service
@RequiredArgsConstructor
public class NotificationDispatchWorker {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    /**
     * 최초 발송: PENDING → PROCESSING → SUCCESS / FAILED / DEAD
     * (단위 테스트·직접 호출용. 운영 폴링은 claim 후 {@link #processClaimed} 사용)
     */
    @Transactional
    public void dispatch(Notification notification) {
        notification.startProcessing();
        notificationRepository.save(notification);
        processClaimed(notification);
    }

    /**
     * 재발송: FAILED → PROCESSING → SUCCESS / FAILED / DEAD
     */
    @Transactional
    public void dispatchRetry(Notification notification) {
        notification.startProcessingForRetry();
        notificationRepository.save(notification);
        processClaimed(notification);
    }

    /**
     * 이미 PROCESSING으로 선점된 알림에 대해 발송만 수행한다.
     */
    @Transactional
    public void processClaimed(Notification notification) {
        if (notification.getStatus() != NotificationStatus.PROCESSING) {
            throw new IllegalStateException(
                    "PROCESSING 상태에서만 발송 가능: 현재=" + notification.getStatus()
            );
        }

        try {
            notificationSender.send(notification);
            notification.markSuccess();
        } catch (Exception e) {
            notification.markFailed(e.getMessage());
        }

        notificationRepository.save(notification);
    }
}
