package liveklass.notification.domain.notification.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.entity.NotificationType;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.notification.sender.NotificationSender;
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

import java.util.List;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatchService 테스트")
class NotificationDispatchServiceTest {

    private static final Long NOTIFICATION_ID = 10L;
    private static final Long RECEIVER_ID = 1L;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSender notificationSender;

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

        lenient().when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("dispatch()")
    class Dispatch {

        @Test
        @DisplayName("Mock 발송 성공 시 PROCESSING → SUCCESS")
        void success() {
            notificationDispatchService.dispatch(pendingNotification);

            assertThat(pendingNotification.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
            assertThat(pendingNotification.getFailureReason()).isNull();
            verify(notificationSender).send(pendingNotification);
            verify(notificationRepository, times(2)).save(pendingNotification);
        }

        @Test
        @DisplayName("발송 실패 시 PROCESSING → FAILED 및 failureReason 기록")
        void fail_onSendError() {
            doThrow(new RuntimeException("SMTP connection failed"))
                    .when(notificationSender).send(pendingNotification);

            notificationDispatchService.dispatch(pendingNotification);

            assertThat(pendingNotification.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(pendingNotification.getFailureReason()).isEqualTo("SMTP connection failed");
        }

        @Test
        @DisplayName("PENDING이 아니면 상태 전이 불가")
        void fail_invalidStatus() {
            pendingNotification.startProcessing();
            pendingNotification.markSuccess();

            assertThatThrownBy(() -> notificationDispatchService.dispatch(pendingNotification))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("dispatchPendingNotifications()")
    class DispatchPending {

        @Test
        @DisplayName("PENDING 목록을 조회해 건별 dispatch 한다")
        void dispatchesAllPending() {
            given(notificationRepository.findByStatusOrderByIdAsc(NotificationStatus.PENDING))
                    .willReturn(List.of(pendingNotification));

            notificationDispatchService.dispatchPendingNotifications();

            assertThat(pendingNotification.getStatus()).isEqualTo(NotificationStatus.SUCCESS);
            verify(notificationRepository).findByStatusOrderByIdAsc(NotificationStatus.PENDING);
        }
    }
}
