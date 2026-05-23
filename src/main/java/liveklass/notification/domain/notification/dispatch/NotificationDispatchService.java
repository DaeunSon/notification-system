package liveklass.notification.domain.notification.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 스케줄러용 발송 오케스트레이션.
 * DB에 남아 있는 PENDING/FAILED를 선점(claim)한 뒤 Worker에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationDispatchClaimService notificationDispatchClaimService;
    private final NotificationDispatchWorker notificationDispatchWorker;

    public void dispatchPendingNotifications() {
        while (true) {
            var claimed = notificationDispatchClaimService.claimNextPending();
            if (claimed.isEmpty()) {
                break;
            }
            notificationDispatchWorker.processClaimed(claimed.get());
        }
    }

    public void dispatchFailedRetryDueNotifications() {
        while (true) {
            var claimed = notificationDispatchClaimService.claimNextFailedRetryDue();
            if (claimed.isEmpty()) {
                break;
            }
            notificationDispatchWorker.processClaimed(claimed.get());
        }
    }
}
