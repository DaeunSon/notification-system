package liveklass.notification.domain.enrollment.service;

import liveklass.notification.domain.enrollment.dto.EnrollCourseRequest;
import liveklass.notification.domain.enrollment.dto.EnrollCourseResponse;
import liveklass.notification.domain.notification.dto.CreateNotificationRequest;
import liveklass.notification.domain.notification.entity.NotificationType;
import liveklass.notification.domain.notification.event.NotificationRequestedEvent;
import liveklass.notification.domain.user.entity.User;
import liveklass.notification.domain.user.repository.UserRepository;
import liveklass.notification.global.exception.BusinessException;
import liveklass.notification.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final String REJECTED_COURSE_ID = "fail-business";

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    /**
     * 수강 등록 성공 시 ENROLLMENT_CONFIRMED 알림 생성
     * 수강 등록 실패 시 ENROLLMENT_REJECTED 알림 생성
     */
    @Transactional
    public EnrollCourseResponse enroll(EnrollCourseRequest request) {
        validateReceiverExists(request.receiverId());

        if (REJECTED_COURSE_ID.equals(request.courseId())) {
            publishNotification(request, NotificationType.ENROLLMENT_REJECTED);
            return EnrollCourseResponse.rejected(request.courseId());
        }

        publishNotification(request, NotificationType.ENROLLMENT_CONFIRMED);
        return EnrollCourseResponse.completed(request.courseId());
    }

    /**
     * 수강 취소 시 ENROLLMENT_CANCELED 알림 생성
     */
    @Transactional
    public EnrollCourseResponse cancel(EnrollCourseRequest request) {
        validateReceiverExists(request.receiverId());

        publishNotification(request, NotificationType.ENROLLMENT_CANCELED);
        return EnrollCourseResponse.canceled(request.courseId());
    }

    private void publishNotification(EnrollCourseRequest request, NotificationType type) {
        eventPublisher.publishEvent(new NotificationRequestedEvent(
                new CreateNotificationRequest(
                        request.receiverId(),
                        type,
                        request.courseId(),
                        request.channel()
                )
        ));
    }

    private void validateReceiverExists(Long receiverId) {
        userRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "수신자 ID: " + receiverId));
    }
}
