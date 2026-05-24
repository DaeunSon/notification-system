package liveklass.notification.domain.notification.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.notification.sender.NotificationSender;
import liveklass.notification.domain.notification.support.NotificationTestFixtures;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatchWorker 테스트")
class NotificationDispatchWorkerTest {

    private static final Long NOTIFICATION_ID = 10L;
    private static final Long RECEIVER_ID = 1L;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private NotificationDispatchWorker notificationDispatchWorker;

    private Notification pendingNotification;

    @BeforeEach
    void setUp() {
        reset(notificationSender);
        User receiver = mock(User.class);
        lenient().when(receiver.getId()).thenReturn(RECEIVER_ID);

        pendingNotification = NotificationTestFixtures.pending(receiver);
        ReflectionTestUtils.setField(pendingNotification, "id", NOTIFICATION_ID);

        lenient().when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("dispatch()")
    class Dispatch {

        @Test
        @DisplayName("Mock 발송 성공 시 PROCESSING → SUCCESS")
        void success() {
            notificationDispatchWorker.dispatch(pendingNotification);

            assertThat(pendingNotification.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
            assertThat(pendingNotification.getFailureReason()).isNull();
            verify(notificationSender).send(pendingNotification);
            verify(notificationRepository, times(2)).save(pendingNotification);
        }

        @Test
        @DisplayName("발송 실패 시 PROCESSING → FAILED 및 failureReason·재시도 예약")
        void fail_onSendError() {
            doThrow(new RuntimeException("SMTP connection failed"))
                    .when(notificationSender).send(pendingNotification);

            notificationDispatchWorker.dispatch(pendingNotification);

            assertThat(pendingNotification.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(pendingNotification.getFailureReason()).isEqualTo("SMTP connection failed");
            assertThat(pendingNotification.getRetryCount()).isEqualTo(1);
            assertThat(pendingNotification.getNextRetryAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING이 아니면 상태 전이 불가")
        void fail_invalidStatus() {
            pendingNotification.startProcessing();
            pendingNotification.markSuccess();

            assertThatThrownBy(() -> notificationDispatchWorker.dispatch(pendingNotification))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("dispatchRetry()")
    class DispatchRetry {

        @Test
        @DisplayName("재시도 발송 성공 시 FAILED → SUCCESS")
        void success() {
            Notification failed = failedNotificationAfterFirstFailure();
            prepareFailedRetryDue(failed, 1);

            notificationDispatchWorker.dispatchRetry(failed);

            assertThat(failed.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
            assertThat(failed.getFailureReason()).isNull();
            assertThat(failed.getNextRetryAt()).isNull();
            verify(notificationSender, times(2)).send(failed);
        }

        @Test
        @DisplayName("재시도 발송 실패 시 retryCount 증가 및 FAILED 유지")
        void fail_onSendError() {
            Notification failed = failedNotificationAfterFirstFailure();
            prepareFailedRetryDue(failed, 1);

            doThrow(new RuntimeException("retry send failed"))
                    .when(notificationSender).send(failed);

            notificationDispatchWorker.dispatchRetry(failed);

            assertThat(failed.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(failed.getRetryCount()).isEqualTo(2);
            assertThat(failed.getFailureReason()).isEqualTo("retry send failed");
            assertThat(failed.getNextRetryAt()).isNotNull();
        }

        @Test
        @DisplayName("retryCount=2 상태에서 재시도 실패 시 DEAD로 전이")
        void fail_thirdAttempt_becomesDead() {
            prepareFailedRetryDue(pendingNotification, 2);
            doThrow(new RuntimeException("final failure"))
                    .when(notificationSender).send(pendingNotification);

            notificationDispatchWorker.dispatchRetry(pendingNotification);

            assertThat(pendingNotification.getStatus()).isEqualTo(NotificationStatus.DEAD);
            assertThat(pendingNotification.getRetryCount()).isEqualTo(3);
            assertThat(pendingNotification.getFailureReason()).isEqualTo("final failure");
            assertThat(pendingNotification.getNextRetryAt()).isNull();
        }

        @Test
        @DisplayName("재시도 시각이 아직 안 됐으면 startProcessingForRetry 불가")
        void fail_retryNotDue() {
            Notification failed = failedNotificationAfterFirstFailure();
            ReflectionTestUtils.setField(failed, "nextRetryAt", LocalDateTime.now().plusMinutes(5));

            assertThatThrownBy(() -> notificationDispatchWorker.dispatchRetry(failed))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private Notification failedNotificationAfterFirstFailure() {
        doThrow(new RuntimeException("first failure"))
                .doNothing()
                .when(notificationSender).send(pendingNotification);

        notificationDispatchWorker.dispatch(pendingNotification);
        return pendingNotification;
    }

    private void prepareFailedRetryDue(Notification notification, int retryCount) {
        ReflectionTestUtils.setField(notification, "status", NotificationStatus.FAILED);
        ReflectionTestUtils.setField(notification, "retryCount", retryCount);
        ReflectionTestUtils.setField(notification, "nextRetryAt", LocalDateTime.now().minusMinutes(1));
    }
}
