package liveklass.notification.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import liveklass.notification.domain.notification.support.NotificationTestFixtures;
import liveklass.notification.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("Notification 수동 재시도")
class NotificationManualRetryTest {

    private Notification notification;

    @BeforeEach
    void setUp() {
        User receiver = Mockito.mock(User.class);
        notification = NotificationTestFixtures.pending(receiver);
        notification.startProcessing();
        ReflectionTestUtils.setField(notification, "retryCount", 2);
        notification.markFailed("send failed");
        ReflectionTestUtils.setField(notification, "stuckRecoveryCount", 2);
    }

    @Test
    @DisplayName("requeueForManualRetry()는 DEAD → PENDING, retryCount=0으로 리셋한다")
    void requeueForManualRetry_resetsToPending() {
        notification.requeueForManualRetry();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isZero();
        assertThat(notification.getFailureReason()).isNull();
        assertThat(notification.getNextRetryAt()).isNull();
        assertThat(notification.getProcessingStartedAt()).isNull();
        assertThat(notification.getStuckRecoveryCount()).isZero();
    }

    @Test
    @DisplayName("DEAD가 아니면 requeueForManualRetry()는 예외를 던진다")
    void requeueForManualRetry_fail_whenNotDead() {
        notification.requeueForManualRetry();

        assertThrows(IllegalStateException.class, notification::requeueForManualRetry);
    }
}
