package liveklass.notification.domain.notification.dispatch;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.notification.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    public void dispatchPendingNotifications() {
        List<Notification> pending =
                notificationRepository.findByStatusOrderByIdAsc(NotificationStatus.PENDING);

        for (Notification notification : pending) {
            dispatch(notification);
        }
    }

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
}
