package liveklass.notification.domain.notification.service;

import liveklass.notification.domain.notification.dto.CreateNotificationRequest;
import liveklass.notification.domain.notification.dto.response.NotificationAcceptedResponse;
import liveklass.notification.domain.notification.dto.response.NotificationDetailResponse;
import liveklass.notification.domain.notification.dto.response.NotificationReadResponse;
import liveklass.notification.domain.notification.dto.response.NotificationRetryResponse;
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

    private final NotificationCreationService notificationCreationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationAcceptedResponse createRequest(CreateNotificationRequest request) {
        return notificationCreationService.createPending(request);
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

    /**
     * 알림 읽음 처리. 이미 읽음이면 멱등하게 200을 반환한다.
     * @param receiverId 지정 시 수신자 일치 여부 검증
     */
    @Transactional
    public NotificationReadResponse markAsRead(Long notificationId, Long receiverId) {
        Notification notification = findNotification(notificationId);

        if (receiverId != null && !notification.getReceiver().getId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED,
                    "수신자 ID: " + receiverId + ", 알림 ID: " + notificationId);
        }

        notification.markAsRead();
        return NotificationReadResponse.from(notification);
    }

    /**
     * DEAD 알림을 PENDING으로 되돌려 스케줄러 재발송 대기열에 넣는다.
     * retryCount는 0으로 리셋한다.
     */
    @Transactional
    public NotificationRetryResponse retryDeadNotification(Long notificationId) {
        Notification notification = findNotification(notificationId);

        if (notification.getStatus() != NotificationStatus.DEAD) {
            throw new BusinessException(
                    ErrorCode.NOTIFICATION_NOT_RETRYABLE,
                    "알림 ID: " + notificationId + ", 현재 상태: " + notification.getStatus()
            );
        }

        notification.requeueForManualRetry();
        return NotificationRetryResponse.from(notification);
    }

    private Notification findNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    private User findReceiver(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "수신자 ID: " + userId));
    }
}

