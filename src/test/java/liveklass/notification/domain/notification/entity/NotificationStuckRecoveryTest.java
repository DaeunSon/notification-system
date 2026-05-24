package liveklass.notification.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import liveklass.notification.domain.notification.support.NotificationTestFixtures;
import liveklass.notification.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Notification 스턱 복구")
class NotificationStuckRecoveryTest {

    private Notification notification;

    @BeforeEach
    void setUp() {
        User receiver = Mockito.mock(User.class);
        notification = NotificationTestFixtures.pending(receiver);
        notification.startProcessing();
    }

    @Test
    @DisplayName("PROCESSING 진입 시 processingStartedAt이 기록된다")
    void recordsProcessingStartedAt() {
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PROCESSING);
        assertThat(notification.getProcessingStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("retryCount=0이면 PENDING으로 복구")
    void recoverToPending() {
        notification.recoverFromStuckProcessing("stuck");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isZero();
        assertThat(notification.getStuckRecoveryCount()).isEqualTo(1);
        assertThat(notification.getProcessingStartedAt()).isNull();
    }

    @Test
    @DisplayName("retryCount>0이면 FAILED로 복구")
    void recoverToFailed() {
        notification.markFailed("previous");
        ReflectionTestUtils.setField(notification, "nextRetryAt", LocalDateTime.now().minusMinutes(1));
        notification.startProcessingForRetry();

        notification.recoverFromStuckProcessing("stuck");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getStuckRecoveryCount()).isEqualTo(1);
        assertThat(notification.isRetryDue()).isTrue();
    }

    @Test
    @DisplayName("스턱 복구 3회째에 DEAD로 전이한다")
    void thirdStuckRecovery_becomesDead() {
        notification.recoverFromStuckProcessing("stuck-1");
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);

        notification.startProcessing();
        notification.recoverFromStuckProcessing("stuck-2");
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);

        notification.startProcessing();
        notification.recoverFromStuckProcessing("stuck-3");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DEAD);
        assertThat(notification.getStuckRecoveryCount()).isEqualTo(3);
        assertThat(notification.getFailureReason()).isEqualTo("stuck-3");
    }

    @Test
    @DisplayName("발송 성공 시 stuckRecoveryCount를 초기화한다")
    void markSuccess_resetsStuckRecoveryCount() {
        notification.recoverFromStuckProcessing("stuck");
        notification.startProcessing();

        notification.markSuccess();

        assertThat(notification.getStuckRecoveryCount()).isZero();
    }
}
