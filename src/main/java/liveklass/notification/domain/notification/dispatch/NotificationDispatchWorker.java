package liveklass.notification.domain.notification.dispatch;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 1건 단위 발송 처리. 별도 빈으로 분리해 {@code @Transactional} 프록시가 적용되도록 한다.
 * (동일 클래스 내부 호출 시 트랜잭션이 적용되지 않음)
 */
@Service
@RequiredArgsConstructor
public class NotificationDispatchWorker {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    /**
     * 최초 발송: PENDING → PROCESSING → SUCCESS / FAILED / DEAD
     */
    @Transactional
    public void dispatch(Notification notification) {
        notification.startProcessing();
        notificationRepository.save(notification);

        try {
            notificationSender.send(notification);
            notification.markSuccess();
        } catch (Exception e) {
            notification.markFailed(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    /**
     * 재발송: FAILED → PROCESSING → SUCCESS / FAILED / DEAD
     */
    @Transactional
    public void dispatchRetry(Notification notification) {
        notification.startProcessingForRetry();
        notificationRepository.save(notification);

        try {
            notificationSender.send(notification);
            notification.markSuccess();
        } catch (Exception e) {
            notification.markFailed(e.getMessage());
        }

        notificationRepository.save(notification);
    }
}
