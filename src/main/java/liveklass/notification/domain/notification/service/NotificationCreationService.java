package liveklass.notification.domain.notification.service;

import liveklass.notification.domain.notification.dto.CreateNotificationRequest;
import liveklass.notification.domain.notification.dto.response.NotificationAcceptedResponse;
import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.user.entity.User;
import liveklass.notification.domain.user.repository.UserRepository;
import liveklass.notification.global.exception.BusinessException;
import liveklass.notification.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationCreationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationAcceptedResponse createPending(CreateNotificationRequest request) {
        User receiver = findReceiver(request.receiverId());

        Notification notification = Notification.createPending(
                receiver,
                request.notificationType(),
                request.referenceId(),
                request.channel()
        );

        validateNotDuplicated(notification);

        return NotificationAcceptedResponse.accepted(
                saveOrThrowDuplicate(notification)
        );
    }

    private User findReceiver(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "수신자 ID: " + userId));
    }

    private Notification saveOrThrowDuplicate(Notification notification) {
        try {
            return notificationRepository.save(notification);
        } catch (DataIntegrityViolationException ex) {
            if (isNotificationDuplicateConstraint(ex)) {
                throw new BusinessException(ErrorCode.NOTIFICATION_DUPLICATE, duplicateDetail(notification));
            }
            throw ex;
        }
    }

    private boolean isNotificationDuplicateConstraint(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause == null || cause.getMessage() == null) {
            return false;
        }
        String message = cause.getMessage();
        return message.contains("uk_notification_recipient_type_reference_channel")
                || message.contains("Duplicate entry")
                || message.contains("duplicate key");
    }

    private void validateNotDuplicated(Notification notification) {
        boolean isDuplicated = notificationRepository.existsByReceiver_IdAndNotificationTypeAndReferenceIdAndChannel(
                notification.getReceiver().getId(),
                notification.getNotificationType(),
                notification.getReferenceId(),
                notification.getChannel()
        );

        if (isDuplicated) {
            throw new BusinessException(ErrorCode.NOTIFICATION_DUPLICATE, duplicateDetail(notification));
        }
    }

    private String duplicateDetail(Notification notification) {
        return "수신자 ID: " + notification.getReceiver().getId()
                + ", 알림 유형: " + notification.getNotificationType()
                + ", 참조 ID: " + notification.getReferenceId()
                + ", 채널: " + notification.getChannel();
    }
}
