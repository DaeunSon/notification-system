package liveklass.notification.domain.notification.repository;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByReceiver_IdAndNotificationTypeAndReferenceIdAndChannel(
            Long receiverId,
            NotificationType notificationType,
            String referenceId,
            NotificationChannel channel
    );

    List<Notification> findByReceiver_IdOrderByIdDesc(Long receiverId);

    List<Notification> findByReceiver_IdAndReadOrderByIdDesc(Long receiverId, boolean read);

    List<Notification> findByStatusOrderByIdAsc(NotificationStatus status);
}
