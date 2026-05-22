package liveklass.notification.domain.notification.service;

import liveklass.notification.domain.notification.dto.CreateNotificationRequest;
import liveklass.notification.domain.notification.dto.response.NotificationAcceptedResponse;
import liveklass.notification.domain.notification.dto.response.NotificationDetailResponse;
import liveklass.notification.domain.notification.dto.response.NotificationStatusResponse;
import liveklass.notification.domain.notification.dto.response.NotificationSummaryResponse;
import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.user.entity.User;
import liveklass.notification.domain.user.repository.UserRepository;
import liveklass.notification.global.exception.BusinessException;
import liveklass.notification.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 발송 요청 등록
     * @param request
     * @return
     */
    @Transactional
    public NotificationAcceptedResponse createRequest(CreateNotificationRequest request) {
        User receiver = findReceiver(request.receiverId());

        Notification notification = Notification.createPending(
                receiver,
                request.notificationType(),
                request.referenceId(),
                request.channel());

        validateNotDuplicated(notification);

        return NotificationAcceptedResponse.accepted(
                notificationRepository.save(notification)
        );
    }

    /**
     * 알림 단건 조회
     */
    @Transactional(readOnly = true)
    public NotificationDetailResponse getNotification(Long notificationId) {
        Notification notification = findNotification(notificationId);
        return NotificationDetailResponse.from(notification);
    }

    /**
     * 알림 상태 조회
     */
    @Transactional(readOnly = true)
    public NotificationStatusResponse getStatus(Long notificationId) {
        return NotificationStatusResponse.from(findNotification(notificationId));
    }

    /**
     * 사용자 알림 목록 조회
     * @Param receiverId
     */
    @Transactional(readOnly = true)
    public List<NotificationSummaryResponse> getNotificationList(Long receiverId, Boolean read) {
        findReceiver(receiverId);

        List<Notification> notifications = read == null
                ? notificationRepository.findByReceiver_IdOrderByIdDesc(receiverId)
                : notificationRepository.findByReceiver_IdAndReadOrderByIdDesc(receiverId, read);

        return notifications.stream()
                .map(NotificationSummaryResponse::from)
                .toList();
    }

    private Notification findNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    private User findReceiver(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "수신자 ID: " + userId));
    }

    private void validateNotDuplicated(Notification notification) {
        boolean isDuplicated =  notificationRepository.existsByReceiver_IdAndNotificationTypeAndReferenceIdAndChannel(
                notification.getReceiver().getId(),
                notification.getNotificationType(),
                notification.getReferenceId(),
                notification.getChannel()
        );

        if (isDuplicated) {
            throw new BusinessException(ErrorCode.NOTIFICATION_DUPLICATE, "수신자 ID: " + notification.getReceiver().getId() +
                    ", 알림 유형: " + notification.getNotificationType() +
                    ", 참조 ID: " + notification.getReferenceId() +
                    ", 채널: " + notification.getChannel());
        }
    }

}


