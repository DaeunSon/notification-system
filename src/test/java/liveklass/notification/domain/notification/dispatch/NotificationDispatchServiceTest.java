package liveklass.notification.domain.notification.dispatch;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.support.NotificationTestFixtures;
import liveklass.notification.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatchService 테스트")
class NotificationDispatchServiceTest {

    private static final Long NOTIFICATION_ID = 10L;
    private static final Long RECEIVER_ID = 1L;

    @Mock
    private NotificationDispatchClaimService notificationDispatchClaimService;

    @Mock
    private NotificationDispatchWorker notificationDispatchWorker;

    @InjectMocks
    private NotificationDispatchService notificationDispatchService;

    private Notification pendingNotification;

    @BeforeEach
    void setUp() {
        User receiver = mock(User.class);
        lenient().when(receiver.getId()).thenReturn(RECEIVER_ID);

        pendingNotification = NotificationTestFixtures.pending(receiver);
        ReflectionTestUtils.setField(pendingNotification, "id", NOTIFICATION_ID);
    }

    @Nested
    @DisplayName("dispatchPendingNotifications()")
    class DispatchPending {

        @Test
        @DisplayName("선점한 PENDING 알림을 processClaimed로 처리한다")
        void dispatchesClaimedPending() {
            Notification processing = pendingNotification;
            processing.startProcessing();

            given(notificationDispatchClaimService.claimNextPending())
                    .willReturn(Optional.of(processing), Optional.empty());

            notificationDispatchService.dispatchPendingNotifications();

            verify(notificationDispatchClaimService, times(2)).claimNextPending();
            verify(notificationDispatchWorker).processClaimed(processing);
        }
    }

    @Nested
    @DisplayName("dispatchFailedRetryDueNotifications()")
    class DispatchFailedRetryDue {

        @Test
        @DisplayName("선점한 FAILED 알림을 processClaimed로 처리한다")
        void dispatchesClaimedFailed() {
            Notification failed = pendingNotification;
            ReflectionTestUtils.setField(failed, "status", NotificationStatus.FAILED);
            ReflectionTestUtils.setField(failed, "retryCount", 1);
            ReflectionTestUtils.setField(failed, "nextRetryAt", java.time.LocalDateTime.now().minusMinutes(1));
            failed.startProcessingForRetry();

            given(notificationDispatchClaimService.claimNextFailedRetryDue())
                    .willReturn(Optional.of(failed), Optional.empty());

            notificationDispatchService.dispatchFailedRetryDueNotifications();

            verify(notificationDispatchClaimService, times(2)).claimNextFailedRetryDue();
            verify(notificationDispatchWorker).processClaimed(failed);
        }
    }
}
