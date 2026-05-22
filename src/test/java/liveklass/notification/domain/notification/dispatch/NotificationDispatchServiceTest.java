package liveklass.notification.domain.notification.dispatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.entity.NotificationType;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatchService 테스트")
class NotificationDispatchServiceTest {

    private static final Long NOTIFICATION_ID = 10L;
    private static final Long RECEIVER_ID = 1L;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDispatchWorker notificationDispatchWorker;

    @InjectMocks
    private NotificationDispatchService notificationDispatchService;

    private Notification pendingNotification;

    @BeforeEach
    void setUp() {
        User receiver = mock(User.class);
        lenient().when(receiver.getId()).thenReturn(RECEIVER_ID);

        pendingNotification = Notification.createPending(
                receiver,
                NotificationType.ENROLLMENT_CONFIRMED,
                "course-1",
                NotificationChannel.EMAIL
        );
        ReflectionTestUtils.setField(pendingNotification, "id", NOTIFICATION_ID);
    }

    @Nested
    @DisplayName("dispatchPendingNotifications()")
    class DispatchPending {

        @Test
        @DisplayName("PENDING 목록을 조회해 Worker에 건별 위임한다")
        void dispatchesAllPending() {
            given(notificationRepository.findByStatusOrderByIdAsc(NotificationStatus.PENDING))
                    .willReturn(List.of(pendingNotification));

            notificationDispatchService.dispatchPendingNotifications();

            verify(notificationRepository).findByStatusOrderByIdAsc(NotificationStatus.PENDING);
            verify(notificationDispatchWorker).dispatch(pendingNotification);
        }
    }

    @Nested
    @DisplayName("dispatchFailedRetryDueNotifications()")
    class DispatchFailedRetryDue {

        @Test
        @DisplayName("재시도 가능한 FAILED 목록을 조회해 Worker에 건별 위임한다")
        void dispatchesAllFailedRetryDue() {
            Notification failed = pendingNotification;
            ReflectionTestUtils.setField(failed, "status", NotificationStatus.FAILED);
            ReflectionTestUtils.setField(failed, "retryCount", 1);
            ReflectionTestUtils.setField(failed, "nextRetryAt", LocalDateTime.now().minusMinutes(1));

            given(notificationRepository.findByStatusAndNextRetryAtBeforeAndRetryCountLessThan(
                    eq(NotificationStatus.FAILED),
                    any(LocalDateTime.class),
                    eq(Notification.MAX_RETRY_COUNT)
            )).willReturn(List.of(failed));

            notificationDispatchService.dispatchFailedRetryDueNotifications();

            verify(notificationRepository).findByStatusAndNextRetryAtBeforeAndRetryCountLessThan(
                    eq(NotificationStatus.FAILED),
                    any(LocalDateTime.class),
                    eq(Notification.MAX_RETRY_COUNT)
            );
            verify(notificationDispatchWorker).dispatchRetry(failed);
        }
    }
}
